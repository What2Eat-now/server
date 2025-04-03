package haru.harudrawer.domain.auth.service.social;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.SocialResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.TokenType;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.service.CommonAuthService;
import haru.harudrawer.domain.auth.service.TokenService;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AppleAuthService extends AbstractSocialAuthService{

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.public-key-url}")
    private String publicKeyUrl;

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.private-key-url}")
    private String privateKeyFileUrl;

    public AppleAuthService(AuthRepository authRepository, TokenService tokenService, CommonAuthService commonAuthService, AuthConverter authConverter, RedisService redisService, RestTemplate restTemplate, JwtProvider jwtProvider) {
        super(authRepository, tokenService, commonAuthService, authConverter, redisService, restTemplate, jwtProvider);
    }


    @Override
    protected SocialRequestDTO.SocialUserInfoDTO getSocialUserInfo(String tokenOrCode) throws Exception {

        // 토큰 조회
        SocialResponseDTO.AppleTokenInfoDTO loginResponse = requestAppleToken(tokenOrCode);

        // idToken에서 사용자 이메일 조회
        String userEmail = extractEmailFromIdToken(loginResponse.getIdToken());

        // apple에서 발급받은 refresh 토큰 저장
        redisService.saveToken(userEmail, loginResponse.getRefreshToken(), Provider.APPLE, TokenType.REFRESH);

        return SocialRequestDTO.SocialUserInfoDTO.builder()
                .userEmail(userEmail)
                .build();
    }

    @Override
    protected Provider getProvider() {
        return Provider.APPLE;
    }

    /*
     * Apple 요청 메소드
     */

    /**
     * 애플 토큰 요청
     */
    private SocialResponseDTO.AppleTokenInfoDTO requestAppleToken(String authorizationCode) throws Exception {

        // clientSecret 생성 (앞서 구현한 makeClientSecret() 메서드 사용)
        String clientSecret = createClientSecret();

        try{
            // 애플 토큰 엔드포인트에 POST 요청
            SocialResponseDTO.AppleTokenInfoDTO response = restTemplate.postForEntity(
                    "https://appleid.apple.com/auth/token",
                    createAppleRequestEntity(Optional.of(authorizationCode), clientSecret, Optional.empty()),
                    SocialResponseDTO.AppleTokenInfoDTO.class).getBody();

            return response;
        } catch (HttpClientErrorException e){
            // 에러 발생 시 로그 출력 및 예외 처리
            log.error("Apple Request Token API 호출 실패: 상태 코드 {}, 응답 본문 {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(AuthErrorCode.APPLE_AUTH_FAILED);
        }
    }

    public void delete() throws Exception {

        revokeAppleToken();

        // user 삭제
        commonAuthService.deleteUser();
    }

    /**
     * 애플 연결 해제 (토큰 회수)
     */
    public void revokeAppleToken() throws Exception {
        String clientSecret = createClientSecret();

        String userEmail = jwtProvider.extractUserEmail();

        Optional<String> refreshToken = redisService.getToken(userEmail, Provider.APPLE, TokenType.REFRESH);

        if (refreshToken.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // Apple 토큰
        try {
            HttpEntity<MultiValueMap<String, String>> appleRequestEntity =
                    createAppleRequestEntity(Optional.empty(), clientSecret, refreshToken);

            // revoke 요청
            restTemplate.postForEntity(
                    "https://appleid.apple.com/auth/revoke",
                    appleRequestEntity,
                    String.class
            );

            log.info("apple 요청 성공");

        } catch (HttpClientErrorException e) {
            log.error("Apple revoke API 호출 실패: 상태 코드 {}, 응답 본문 {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(AuthErrorCode.APPLE_AUTH_FAILED);
        }
    }

    /**
      * 요청 파라미터 준비 (application/x-www-form-urlencoded)
      */
    private HttpEntity<MultiValueMap<String, String>> createAppleRequestEntity(Optional<String> authorizationCode, String clientSecret, Optional<String> refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "authorization_code");

        authorizationCode.ifPresent(code -> params.add("code", code));

        refreshToken.ifPresent(token -> {
            params.add("token", token);
            params.add("token_type_hint", "refresh_token");
            log.info(token);
        });

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        return requestEntity;
    }

    /*
     Apple 요청을 위한 Client Secret 생성 및 관련 메소드
     */

    /**
     * client Secret 생성
     */
    private String createClientSecret() throws Exception {
        // 만료일 생성
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    /**
     * ClientSecret에 사용할 privateKey 생성
     */
    private PrivateKey getPrivateKey() throws Exception {
        // BouncyCastle Provider 추가 (이미 추가되어 있다면 생략 가능)

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        String privateKeyId = readFile();

        // PEMParser를 사용해 PEM 문자열을 파싱합니다.
        PEMParser pemParser = new PEMParser(new StringReader(privateKeyId));
        Object object = pemParser.readObject();
        pemParser.close();

        // JcaPEMKeyConverter를 통해 PrivateKey 객체로 변환
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey;

        if (object instanceof PrivateKeyInfo privateKeyInfo) {
            privateKey = converter.getPrivateKey(privateKeyInfo);
        } else {
            throw new AuthException(AuthErrorCode.APPLE_UNSUPPORTED_KEY_TYPE);
        }

        return privateKey;
    }

    // 애플 공개키 파싱
    public PublicKey getApplePublicKey(String kid) throws Exception {

        // Apple 공개키 엔드포인트에서 JWKS 가져오기
        JWKSet jwkSet = JWKSet.load(new URL(publicKeyUrl));
        List<JWK> keys = jwkSet.getKeys();

        for (JWK jwk : keys) {
            if (jwk.getKeyID().equals(kid)) {

                // 공개 키의 JWK 부분 추출 후, RSAKey 타입인지 확인하고 RSAPublicKey 반환
                JWK publicJwk = jwk.toPublicJWK();
                if (publicJwk instanceof RSAKey rsaKey) {
                    return rsaKey.toRSAPublicKey();
                } else {
                    throw new AuthException(AuthErrorCode.APPLE_INVALID_KEY_TYPE);
                }
            }
        }
        throw new AuthException(AuthErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
    }

    /**
     * AppleIdToken에서 사용자 이메일 추출
     *
     */
    private String extractEmailFromIdToken(String idToken) throws Exception {
        // idToken -> jwt 형식으로 파싱
        SignedJWT signedJWT = SignedJWT.parse(idToken);

        // 공개키로 id Token 인증후 email 조회
        Claims claims = Jwts.parser()
                .setSigningKey(getApplePublicKey(signedJWT.getHeader().getKeyID()))  // 서명 검증을 위한 키
                .build()
                .parseClaimsJws(idToken)
                .getBody();

        return claims.get("email", String.class);
    }


    /**
     * 파일 읽기
     */
    public String readFile() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(privateKeyFileUrl), StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            sb.append(line);
            sb.append("\n");
        }

        return String.valueOf(sb);
    }
}