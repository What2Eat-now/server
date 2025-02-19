package what.what2eat.domain.auth.service;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import what.what2eat.domain.auth.controller.dto.request.AppleRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.AppleResponseDTO;
import what.what2eat.domain.auth.converter.AuthConverter;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.AuthException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.domain.CustomUserDetails;
import what.what2eat.global.security.jwt.JwtProvider;

import java.io.StringReader;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class AppleAuthService {

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.private-key-id}")
    private String privateKeyId;

    @Value("${apple.public-key-url}")
    private String publicKeyUrl;

    @Value("${apple.team-id}")
    private String teamId;

    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final AuthConverter authConverter;

    public void signup(AppleRequestDTO.AppleSignupDTO request) {
        authRepository.save(authConverter.signupToAppleUserEntity(request));
    }


    // 애플 로그인
    public AppleResponseDTO.AppleLoginResponseDTO login(String authorizationCode) throws Exception {

        // 토큰 조회
        AppleResponseDTO.AppleTokenInfoDTO loginResponse = requestAppleToken(authorizationCode);

        // idToken에서 사용자 이메일 조회
        String userEmail = extractEmailFromIdToken(loginResponse.getIdToken());

        // 사용자 존재 유무 확인
        Optional<User> userOpt = authRepository.findByUserEmail(userEmail);

        if (userOpt.isPresent() && userOpt.get().getProvider().equals(Provider.LOCAL)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL);
        }

        // 사용자 존재하지 않을경우 회원 가입으로 리다이렉트 처리
        if (userOpt.isEmpty()) {
            return AppleResponseDTO.AppleLoginResponseDTO.builder()
                    .appleEmail(userEmail)
                    .requireSignup(true)
                    .accessToken(null)
                    .refreshToken(null)
                    .build();
        }

        User user = userOpt.get();

        String accessToken = createAccessToken(user);

        String refreshToken = jwtProvider.createRefreshToken(user.getUserEmail());

        return AppleResponseDTO.AppleLoginResponseDTO.builder()
                .requireSignup(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .appleEmail(null)
                .build();
    }

    private String createAccessToken(User user) {
        return jwtProvider.createAccessToken(CustomUserDetails.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .password(null)
                .provider(user.getProvider())
                .nickName(user.getNickName())
                .build());
    }

    // 애플 토큰 요청
    private AppleResponseDTO.AppleTokenInfoDTO requestAppleToken(String authorizationCode) throws Exception {

        // 1. clientSecret 생성 (앞서 구현한 makeClientSecret() 메서드 사용)
        String clientSecret = createClientSecret();

        // 2. 요청 파라미터 준비 (application/x-www-form-urlencoded)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");

        // 3. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // 4. 애플 토큰 엔드포인트에 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AppleResponseDTO.AppleTokenInfoDTO> response = restTemplate.postForEntity(
                "https://appleid.apple.com/auth/token",
                requestEntity,
                AppleResponseDTO.AppleTokenInfoDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            // 에러 발생 시 로그 출력 및 예외 처리
            throw new Exception("애플 토큰 요청 실패: " + response.getStatusCode());
        }
    }

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

    private PrivateKey getPrivateKey() throws Exception {
        // BouncyCastle Provider 추가 (이미 추가되어 있다면 생략 가능)

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // PEM 파싱 가능하도록 \n 문자 추가
        privateKeyId = privateKeyId.replace("-----BEGIN PRIVATE KEY-----", "-----BEGIN PRIVATE KEY-----\n")
                .replace("-----END PRIVATE KEY-----", "\n-----END PRIVATE KEY-----");

        // PEMParser를 사용해 PEM 문자열을 파싱합니다.
        PEMParser pemParser = new PEMParser(new StringReader(privateKeyId));
        Object object = pemParser.readObject();
        pemParser.close();

        // JcaPEMKeyConverter를 통해 PrivateKey 객체로 변환
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey;

        if (object instanceof PrivateKeyInfo privateKeyInfo) {
            privateKey = converter.getPrivateKey((privateKeyInfo));
        } else {
            throw new IllegalArgumentException("지원하지 않는 키 형식: " + object.getClass().getName());
        }

        return privateKey;
    }

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
                    throw new Exception("지원하지 않는 키 타입: " + publicJwk.getKeyType());
                }
            }
        }
        throw new Exception("kid에 해당하는 공개키를 찾을 수 없습니다: " + kid);
    }

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
}
