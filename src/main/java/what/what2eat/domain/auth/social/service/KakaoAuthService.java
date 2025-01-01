package what.what2eat.domain.auth.social.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    // 토큰 요청을 위한 Http 요청 객체 생성
    public HttpEntity<MultiValueMap<String, String>> createTokenRequest(String code) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_url", kakaoRedirectUrl);
        params.add("code", code);

        return new HttpEntity<>(params, headers);
    }

    // 인가 코드 정보로 사용자 정보 저장하고있는 access, refresh token 조회
    public KakaoAuthResponseDTO.KakaoTokenDTO getAccessToken(String code) {
        KakaoAuthResponseDTO.KakaoTokenDTO tokenDTO = restTemplate.exchange(
                        "https://kauth.kakao.com/oauth/token",
                        HttpMethod.POST,
                        createTokenRequest(code),
                        KakaoAuthResponseDTO.KakaoTokenDTO.class)
                .getBody();

        return tokenDTO;
    }

    // 토큰으로 사용자 정보 조회
    public KakaoAuthResponseDTO.LoginInfoDTO getKakaoUserInfo(String code) {

        // 토큰 조회
        KakaoAuthResponseDTO.KakaoTokenDTO tokenDTO = getAccessToken(code);
        String kakaoToken = tokenDTO.getAccessToken();

        // 인증을 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoToken);
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
        String accessToken = jwtProvider.createAccessToken(userInfo.getKakaoAccount().getKakaoEmail(), Role.USER);

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
        User findEmail = authRepository.findByUserEmail(userInfo.getKakaoAccount().getKakaoEmail()).get();

        if (findEmail != null && findEmail.getProvider() == Provider.KAKAO) {
            return true;
        }

        return false;
    }



}
