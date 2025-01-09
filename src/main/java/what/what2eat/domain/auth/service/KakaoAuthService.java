package what.what2eat.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.controller.dto.KakaoAuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.converter.KakaoAuthConverter;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.exception.CustomException;
import what.what2eat.global.exception.ErrorCode;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;
import what.what2eat.global.security.jwt.JwtProvider;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KakaoAuthService {

    private final RestTemplate restTemplate;
    private final KakaoAuthConverter kakaoAuthConverter;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;

    // 회원가입
    public void signup(KakaoAuthRequestDTO.KakaoSignupDTO request) {

        // 이메일 유효성 검사
        if (validateKakaoAuth(request.getUserEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        //객체 변환후 저장
        User user = kakaoAuthConverter.signupToUserEntity(request);
        authRepository.save(user);
    }

    // 토큰으로 사용자 정보 조회
    public ApiResponse<Map<String, Object>> login(String kakaoAccessToken) {

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
        if (!validateKakaoAuth(userInfo.getKakaoAccount().getKakaoEmail())) {
            // 회원가입 필요 리다이렉트 처리
            Map<String, Object> data = Map.of(
                    "kakaoUserInfo", userInfo.getKakaoAccount().getKakaoEmail(),
                    "redirectUrl", "/api/v1/auth/signup/kakao",
                    "socialId", userInfo.getUserId()
            );

            return ApiResponse.of(HttpStatus.TEMPORARY_REDIRECT, data);
        }

        // accessToken 생성
        String accessToken = jwtProvider.createAccessToken(userInfo.getKakaoAccount().getKakaoEmail(), Role.USER, Provider.KAKAO);

        // refreshToken 생성
        String refreshToken = jwtProvider.createRefreshToken(userInfo.getKakaoAccount().getKakaoEmail());

        Map<String, Object> tokens = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );

        return ApiResponse.ok(tokens);
    }

    // 카카오 로그인 정보 DB 저장 유무 확인

    public boolean validateKakaoAuth(String kakaoUserEmail) {
        // 계정이 존재할 경우
        if (authRepository.existsByUserEmail(kakaoUserEmail)) {
            return true;
        }
        // 존재하지 않을 경우
        return false;
    }

}
