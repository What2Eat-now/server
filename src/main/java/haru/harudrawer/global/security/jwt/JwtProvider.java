package haru.harudrawer.global.security.jwt;

import haru.harudrawer.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.global.security.domain.CustomUserDetails;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidity; // 1시간

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidity; // 7일

    // Access Token 생성
    public String createAccessToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(accessTokenValidity);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("provider",userDetails.getProvider())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(extractSecretKey())
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userEmail) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(refreshTokenValidity);

        return Jwts.builder()
                .subject(userEmail)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(extractSecretKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(extractSecretKey())
                    .build()
                    .parseSignedClaims(token); //토큰을 파싱하면서, 내부적으로 서명(Signature) 검증과 토큰의 구조가 올바른지 확인
            return true;
        } catch (ExpiredJwtException e) { // 토큰 만료된 경우
            log.error("Expired JWT token: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) { // 유효하지 않은 토큰인 경우
            log.error("Invalid JWT token : {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Spring Security Context에서 userId 추출
    public Long extractUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getUserId();  // userId를 Long 타입으로 변환
    }

    // 토큰에서 클레임 파싱
    private Claims parseClaims(String token) {
        validateToken(token);

        return Jwts.parser()
                .verifyWith(extractSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * SecretKey 추출
     */
    private SecretKey extractSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

}
