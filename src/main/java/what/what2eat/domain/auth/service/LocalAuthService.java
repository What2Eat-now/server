package what.what2eat.domain.auth.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.controller.dto.request.LocalRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.LocalResponseDTO;
import what.what2eat.domain.auth.entity.*;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.AuthException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.domain.auth.repository.EmailRepository;
import what.what2eat.global.security.domain.CustomUserDetails;
import what.what2eat.global.security.jwt.JwtProvider;

import java.time.LocalDateTime;

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

    // 회원가입 =>
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
            .nickName(request.getNickName())
            .role(Role.USER)
            .provider(Provider.LOCAL)
            .userStatus(UserStatus.ACTIVE)
            .build());

        // 인증 객체 삭제
        emailRepository.delete(byUserEmail);

    }

    // 인증번호 이메일 전송
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

    // 인증번호 검증
    public void verifyCode(LocalRequestDTO.VerifyCodeDTO request) {
        log.info("code = " + request.getCode());

        // 인증 토큰 검증
        if (!emailRepository.existsByVerificationCode(request.getCode())) {
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

    public LocalResponseDTO.LocalLoginResponseDTO login(LocalRequestDTO.LoginRequestDTO request) throws Exception {

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

            String accessToken = jwtProvider.createAccessToken(userDetails);

            String refreshToken = jwtProvider.createRefreshToken(request.getUserEmail());

            return LocalResponseDTO.LocalLoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthenticationException e) {
            // 6. 인증 실패
            log.error("인증 실패 : " + e);
            throw new Exception(e);
        }
    }

    /**
     * 검증 메서드
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[@$!%*?&]).{8,16}$");
    }


    // 로그인시
    public void validateMember(String userEmail) {

        // 사용자 조회
        User user = authRepository.findByUserEmailAndUserStatus(userEmail, UserStatus.ACTIVE).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 카카오 로그인으로 이미 가입된 경우
        if (user.getProvider().equals(Provider.KAKAO)) {
            throw new AuthException(AuthErrorCode.ALREADY_EXIST_SOCIAL_EMAIL);
        }

    }
}
