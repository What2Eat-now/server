package what.what2eat.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
import what.what2eat.domain.auth.converter.AuthConverter;
import what.what2eat.domain.auth.entity.SocialType;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${kakao.rest.api.key}")
    private String kakaoClientId;

    @Value("${kakao.redirect_url}")
    private String kakaoRedirectUrl;

    private final RestTemplate restTemplate;
    private final AuthConverter authConverter;
    private final AuthRepository authRepository;

    //카카오 로그인 화면 URL 생성
    public String buildKakaoAuthUrl() {
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUrl)
                .toUriString();
    }

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
    public AuthResponseDTO.TokenDTO getAccessToken(String code) {
        AuthResponseDTO.TokenDTO tokenDTO = restTemplate.exchange(
                        "https://kauth.kakao.com/oauth/token",
                        HttpMethod.POST,
                        createTokenRequest(code),
                        AuthResponseDTO.TokenDTO.class)
                .getBody();

        return tokenDTO;
    }

    // 토큰으로 사용자 정보 조회
    public AuthResponseDTO.LoginInfoDTO getKakaoUserInfo(String code) {

        // 토큰 조회
        AuthResponseDTO.TokenDTO tokenDTO = getAccessToken(code);
        String accessToken = tokenDTO.getAccessToken();

        // 인증을 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);

        // 사용자 정보 조회
        AuthResponseDTO.UserInfoDTO userInfo = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                httpEntity,
                AuthResponseDTO.UserInfoDTO.class).getBody();

        // 사용자 정보 DB 존재 저장 유무 확인
        if(!validateKakaoAuth(userInfo)){
            // 저장을 위해 DTO -> Entity로 convert
            User user = authConverter.kakaoToUserEntity(userInfo);

            // 데이터 저장
            authRepository.save(user);
        }

        return AuthResponseDTO.LoginInfoDTO.builder()
                .token(tokenDTO)
                .userInfo(userInfo)
                .build();
    }


    // 카카오 로그인 정보 DB 저장 유무 확인
    public boolean validateKakaoAuth(AuthResponseDTO.UserInfoDTO userInfo) {
        User findEmail = authRepository.findByUserEmail(userInfo.getKakaoAccount().getKakaoEmail());

        if (findEmail != null && findEmail.getSocialType() == SocialType.KAKAO) {
            return true;
        }

        return false;
    }



}
