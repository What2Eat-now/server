package haru.harudrawer.domain.auth.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import haru.harudrawer.domain.auth.controller.dto.request.LocalRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.entity.*;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.repository.EmailRepository;
import haru.harudrawer.global.s3.S3Service;
import haru.harudrawer.global.security.domain.CustomUserDetails;
import haru.harudrawer.global.security.jwt.JwtProvider;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocalAuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final S3Service s3Service;

    /**
     * 로컬 회원 가입
     */
    public void signUp(LocalRequestDTO.SignUpRequestDTO request){
        // 인증번호 엔티티에서 이메일과 인증 상태로 조회
        EmailVerificationCode byUserEmail = emailRepository.findByUserEmailAndEmailStatus(request.getUserEmail(), true)
                .orElseThrow(() -> new AuthException(AuthErrorCode.NEED_VERIFICATION));

        // 인증 상태가 false인 경우
        if (!byUserEmail.isEmailStatus()) {
            throw new AuthException(AuthErrorCode.NEED_VERIFICATION);
        }

        // 비밀번호 형식 확인
        if (!isValidPassword(request.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }

        // 유저 정보 저장
        authRepository.save(User.builder()
                .userEmail(request.getUserEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .nickName(request.getNickName())
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build());

        // 인증 객체 삭제
        emailRepository.delete(byUserEmail);

    }

    /**
     * 로컬 로그인
     */
    public CommonResponseDTO.LoginResponseDTO login(LocalRequestDTO.LoginRequestDTO request) throws Exception {

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserEmail(),
                            request.getPassword()
                    )
            );

            // 인증 객체에서 사용자 정보 추출(Provider 추출 위해 작성)
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 로컬로 가입한 유저가 아닐경우 에러 처리
            if (!userDetails.getProvider().equals(Provider.LOCAL)) {
                throw new AuthException(AuthErrorCode.ALREADY_EXIST_SOCIAL_EMAIL);
            }

            String accessToken = jwtProvider.createAccessToken(userDetails);

            String refreshToken = jwtProvider.createRefreshToken(request.getUserEmail());

            return CommonResponseDTO.LoginResponseDTO.builder()
                    .tokens(CommonResponseDTO.TokenDTO.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build())
                    .build();

        } catch (InternalAuthenticationServiceException e) {
            // 사용자를 찾을 수 없는 경우 (UsernameNotFoundException이 내부적으로 발생했을 때)
            throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
        }
        catch (BadCredentialsException e) {
            // 비밀번호가 틀린 경우
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
        catch (AuthenticationException e) {
            // 6. 인증 실패
            log.error("인증 실패 : " + e);
            throw new Exception(e);
        }
    }

    /**
     * 회원 탈퇴
     */
    public void delete() {

        User user = authRepository.findByUserId(jwtProvider.extractUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 탈퇴 회원이 저장한 사진 전체 삭제
        s3Service.deleteUserImgList(user);

        // 회원 탈퇴 처리
        authRepository.delete(user);
    }




    /**
     * 인증번호 이메일 전송
     */
    public void sendEmail(String userEmail) throws MessagingException {

        // 이메일 전송 후 인증번호 반환
        String code = emailService.sendVerificationEmail(userEmail);

        // 이메일 정보 저장
        emailRepository.save(EmailVerificationCode.builder()
                .userEmail(userEmail)
                .emailStatus(false)
                .verificationCode(code)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build());
    }

    /**
     * 인증번호 검증
     */
    public void verifyCode(LocalRequestDTO.VerifyCodeDTO request) {
        // 인증 토큰 검증
        if (Boolean.FALSE.equals(emailRepository.existsByVerificationCode(request.getCode()))) {
            throw new AuthException(AuthErrorCode.INVALID_CERTIFICATION_CODE);
        }

        // 인증 번호와 이메일로 저장된 정보 찾기
        EmailVerificationCode findCode = emailRepository.findByUserEmailAndVerificationCodeAndEmailStatus(request.getUserEmail(), request.getCode(), false)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 인증 코드 시간 만료된 경우
        if (findCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        // 인증 상태 변경
        findCode.changeStatus();
    }

    /**
     * 검증 메서드
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[@$!%*?&]).{8,16}$");
    }

    // 회원 가입시 이메일 중복 검사
    public void validateEmailForSignup(String userEmail) {
        if (authRepository.existsByUserEmail(userEmail)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL);
        }
    }

    // 비밀번호 찾기 시 이메일 유효성 검사
    public void validateEmailForRecovery(String userEmail) {
        User user = authRepository.findByUserEmail(userEmail).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 소셜 로그인으로 진행된 이메일인 경우
        if (user.getProvider().equals(Provider.APPLE) || user.getProvider().equals(Provider.KAKAO)) {
            throw new AuthException(AuthErrorCode.ALREADY_EXIST_SOCIAL_EMAIL);
        }
    }


}
