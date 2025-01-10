package what.what2eat.global.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;

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
    public String createAccessToken(String userEmail, Role role, Provider provider) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(accessTokenValidity);

        return Jwts.builder()
                .subject(userEmail)
                .claim("provider",provider.name())
                .claim("role", role)
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

    /**
     * 토큰에서 username 추출
     */
    public String getUserEmail(String token) {
        return Jwts.parser()
                .verifyWith(extractSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
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
