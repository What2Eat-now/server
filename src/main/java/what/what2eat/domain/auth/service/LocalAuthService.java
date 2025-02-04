package what.what2eat.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.controller.dto.request.LocalRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.LocalResponseDTO;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.AuthException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.domain.CustomUserDetails;
import what.what2eat.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocalAuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;




    // 회원가입 =>
    public void signUp(LocalRequestDTO.SignUpRequestDTO request) {

        // 비밀번호 형식 확인
        if (!isValidPassword(request.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }

        authRepository.save(User.builder()
            .userEmail(request.getUserEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickName(request.getNickName())
            .role(Role.USER)
            .provider(Provider.LOCAL)
            .userStatus(UserStatus.ACTIVE)
            .build());
    }

    public LocalResponseDTO.LocalLoginResponseDTO login(LocalRequestDTO.LoginRequestDTO request) throws Exception {

        validateMember(request.getUserEmail());


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
        Boolean isExist = authRepository.existsByUserEmailAndUserStatusAndProvider(userEmail, UserStatus.ACTIVE, Provider.LOCAL);

        // 이메일이 존재하지 않으면 404 에러 반환
        if(!isExist) throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
    }
}
