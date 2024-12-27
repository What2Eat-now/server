package what.what2eat.domain.auth.local.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.local.controller.dto.LocalAuthRequestDTO;
import what.what2eat.domain.auth.local.controller.dto.LocalAuthResponseDTO;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalAuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public void signUp(LocalAuthRequestDTO.SignUpRequestDTO request) {
        if(!authRepository.existsByUserEmailAndProvider(request.getUsername(), Provider.LOCAL)){
            authRepository.save(User.builder()
                    .userEmail(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickName(request.getNickName())
                    .role(Role.USER)
                    .provider(Provider.LOCAL)
                    .build());
        }else{
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
    }

    public LocalAuthResponseDTO.LoginResponseDTO login(LocalAuthRequestDTO.LoginRequestDTO request) throws Exception {
        // 유효성 검사
        validateMember(request);

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 인증 토큰 생성
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse(Role.USER.name());

            String accessToken = jwtProvider.createAccessToken(request.getUsername(), Role.valueOf(role));

            String refreshToken = jwtProvider.createRefreshToken(request.getUsername());


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
        authRepository.existsByUserEmailAndProvider(request.getUsername(), Provider.LOCAL);
    }
}
