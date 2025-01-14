package what.what2eat.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import what.what2eat.global.security.domain.CustomUserDetails;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidity; // 1시간

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidity; // 7일

    private Set<String> blackList = new HashSet<>();

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
        if (isTokenBlackListed(token)) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(extractSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token : {}", e.getMessage());
            return false;
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

    // token blackList 추가
    public void addTokenToBlackList(String token) {
        blackList.add(token);
    }


    // blackList에 토큰 있는지 검사
    public boolean isTokenBlackListed(String token) {
        return blackList.contains(token);
    }
}
