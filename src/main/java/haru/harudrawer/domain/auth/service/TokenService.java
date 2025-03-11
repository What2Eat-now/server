package haru.harudrawer.domain.auth.service;

import haru.harudrawer.domain.auth.controller.dto.request.CommonRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.controller.dto.response.LocalResponseDTO;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.TokenType;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.domain.CustomUserDetails;
import haru.harudrawer.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final AuthRepository authRepository;

    public CommonResponseDTO.TokenDTO createTokens(User user) {

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .provider(user.getProvider())
                .nickName(user.getNickName())
                .password(user.getProvider().equals(Provider.LOCAL) ? user.getPassword() : null)
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().name())))
                .build();

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(user.getUserEmail());

        redisService.saveToken(user.getUserEmail(), refreshToken, user.getProvider(), TokenType.SERVER);

        return CommonResponseDTO.TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 서버 자체 Refresh Token 삭제
     */
    public void deleteRefreshToken(String refreshToken, User user) {
        redisService.deleteRefreshToken(refreshToken, user.getProvider(), TokenType.SERVER);
    }

    /**
     * 서버 자체 refresh Token으로 Access Token 재발급
     */
    public CommonResponseDTO.LoginResponseDTO refreshToken(CommonRequestDTO.TokenRefreshDTO request) {
        // 사용자 이메일 조회
        String userEmail = jwtProvider.getUserEmail(request.getRefreshToken());

        // 이메일로 사용자 정보 DB 조회
        User user = authRepository.findByUserEmail(userEmail).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // redis에서 refresh token 조회
        Optional<String> findTokenOpt = redisService.getToken(userEmail, user.getProvider(), TokenType.SERVER);

        // refresh token 검증
        if (findTokenOpt.isEmpty() || !findTokenOpt.get().equals(request.getRefreshToken())) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // redis에서 만료된 RefreshToken 삭제
        deleteRefreshToken(request.getRefreshToken(), user);

        // 새 토큰 생성
        CommonResponseDTO.TokenDTO tokens = createTokens(user);

        return CommonResponseDTO.LoginResponseDTO.builder()
                .tokens(tokens)
                .build();
    }


    /**
     * 토큰 검증
     */
    public void validateToken(HttpServletRequest request) {
        String token = resolveToken(request);

        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }


    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
