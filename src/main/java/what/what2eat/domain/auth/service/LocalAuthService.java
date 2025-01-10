package what.what2eat.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.controller.dto.LocalAuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.LocalAuthResponseDTO;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.MemberException;
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

    public void signUp(LocalAuthRequestDTO.SignUpRequestDTO request) {
        if (!authRepository.existsByUserEmail(request.getUserEmail())) {
            authRepository.save(User.builder()
                    .userEmail(request.getUserEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickName(request.getNickName())
                    .role(Role.USER)
                    .provider(Provider.LOCAL)
                    .build());
        } else {
            throw new MemberException(AuthErrorCode.DUPLICATE_USER_EMAIL);
        }
    }

    public LocalAuthResponseDTO.LoginResponseDTO login(LocalAuthRequestDTO.LoginRequestDTO request) throws Exception {

        // 유효성 검사
        validateMember(request);

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserEmail(),
                            request.getPassword()
                    )
            );

            // 인증 객체에서 role 추출
            Role role = Role.valueOf(authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse(Role.USER.name()));

            // 인증 객체에서 사용자 정보 추출(Provider 추출 위해 작성)
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtProvider.createAccessToken(request.getUserEmail(), role, userDetails.getProvider());

            String refreshToken = jwtProvider.createRefreshToken(request.getUserEmail());

            return LocalAuthResponseDTO.LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthenticationException e) {
            // 6. 인증 실패
            log.error("인증 실패 : " + e);
            throw new Exception(e);
        }
    }

    private void validateMember(LocalAuthRequestDTO.LoginRequestDTO request) {
        authRepository.existsByUserEmail(request.getUserEmail());
    }


}
