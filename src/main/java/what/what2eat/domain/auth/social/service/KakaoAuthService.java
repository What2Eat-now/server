package what.what2eat.domain.auth.social.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.social.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.social.converter.KakaoAuthConverter;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {

    @Value("${kakao.rest.api.key}")
    private String kakaoClientId;

    @Value("${kakao.redirect_url}")
    private String kakaoRedirectUrl;

    private final RestTemplate restTemplate;
    private final KakaoAuthConverter kakaoAuthConverter;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;


    // 토큰으로 사용자 정보 조회
    public KakaoAuthResponseDTO.LoginInfoDTO getKakaoUserInfo(String kakaoAccessToken) {

        // 인증을 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);

        // 사용자 정보 조회
        KakaoAuthResponseDTO.KakaoUserInfoDTO userInfo = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                httpEntity,
                KakaoAuthResponseDTO.KakaoUserInfoDTO.class).getBody();

        // 사용자 정보 DB 존재 저장 유무 확인
        if(!validateKakaoAuth(userInfo)){
            // 저장을 위해 DTO -> Entity로 convert
            User user = kakaoAuthConverter.kakaoToUserEntity(userInfo);

            // 데이터 저장
            authRepository.save(user);
        }

        // accessToken 생성
        String accessToken = jwtProvider.createAccessToken(userInfo.getKakaoAccount().getKakaoEmail(), Role.USER, Provider.KAKAO);

        // refreshToken 생성
        String refreshToken = jwtProvider.createRefreshToken(userInfo.getKakaoAccount().getKakaoEmail());


        return KakaoAuthResponseDTO.LoginInfoDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }


    // 카카오 로그인 정보 DB 저장 유무 확인
    public boolean validateKakaoAuth(KakaoAuthResponseDTO.KakaoUserInfoDTO userInfo) {
        String kakaoUserEmail = userInfo.getKakaoAccount().getKakaoEmail();

        // 계정이 존재할 경우
        if (authRepository.existsByUserEmailAndProvider(kakaoUserEmail,Provider.KAKAO)) {
            return true;
        }

        // 존재하지 않을 경우
        return false;
    }

    public void logout(HttpServletRequest request) {
        String token = resolveToken(request);

        if (!jwtProvider.validateToken(token)) {
            throw new RuntimeException("이미 블랙리스트에 존재합니다.");
        }
        jwtProvider.addTokenToBlackList(token);
    }

    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
