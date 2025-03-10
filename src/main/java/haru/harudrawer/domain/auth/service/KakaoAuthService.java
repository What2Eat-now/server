package haru.harudrawer.domain.auth.service;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.SocialResponseDTO;
import haru.harudrawer.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.global.exception.CommonErrorCode;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KakaoAuthService{

    private final RestTemplate restTemplate;
    private final AuthConverter authConverter;
    private final AuthRepository authRepository;
    private final RedisService redisService;
    private final TokenService tokenService;
    private final CommonAuthService commonAuthService;

    // 회원가입
    public void signup(SocialRequestDTO.SocialSignupDTO request) {
        //객체 변환후 저장
        authRepository.save(authConverter.signupToKakaoUserEntity(request));

    }

    // 토큰으로 사용자 정보 조회
    public CommonResponseDTO.LoginResponseDTO login(String kakaoAccessToken) {

        // 사용자 정보 조회
        SocialResponseDTO.KakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoAccessToken);

        // 사용자 존재 유무 확인
        Optional<User> userOpt = authRepository.findByUserEmail(userInfo.getKakaoAccount().getKakaoEmail());

        // 사용자가 존재하지만 로컬 or 애플로 가입된 회원인지 확인
        if (userOpt.isPresent() &&
                (userOpt.get().getProvider().equals(Provider.LOCAL) ||
                        userOpt.get().getProvider().equals(Provider.APPLE))) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL);
        }

        // 사용자 존재하지 않을경우 회원 가입으로 리다이렉트 처리
        if (userOpt.isEmpty()) {
            return CommonResponseDTO.LoginResponseDTO.builder()
                    .requiresSignup(true)
                    .userEmail(userInfo.getKakaoAccount().getKakaoEmail())
                    .tokens(null)
                    .build();
        }

        // 로그인 성공
        User user = userOpt.get();

        CommonResponseDTO.TokenDTO tokens = tokenService.createTokens(user);

        // redis에 refresh token 저장
        redisService.saveRefreshToken(user.getUserEmail(), tokens.getRefreshToken());

        return CommonResponseDTO.LoginResponseDTO.builder()
                .requiresSignup(false)
                .userEmail(null)
                .tokens(tokens)
                .build();
    }

    /**
     * 카카오 회원 탈퇴
     */
    public void delete(String kakaoAccessToken) {
        // 카카오 연결 해제
        unlinkKakaoAccount(kakaoAccessToken);

        // 회원 삭제
        commonAuthService.deleteUser();
    }

    /**
     * 카카오 연결 해제(약관 동의 회수)
     */
    private void unlinkKakaoAccount(String accessToken) {
        try {
            restTemplate.postForEntity(
                    "https://kapi.kakao.com/v1/user/unlink",
                    createKakaoRequestEntity(accessToken),
                    String.class
            ).getBody();

        } catch (HttpClientErrorException e) {
            log.error("Kakao 연결 해제 API 호출 실패: 상태 코드 {}, 응답 본문 {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(CommonErrorCode.BAD_REQUEST);
        }
    }

    // 카카오 사용자 정보 조회
    private SocialResponseDTO.KakaoUserInfoDTO getKakaoUserInfo(String kakaoAccessToken) {
        try {
            return restTemplate.postForEntity(
                    "https://kapi.kakao.com/v2/user/me",
                    createKakaoRequestEntity(kakaoAccessToken),
                    SocialResponseDTO.KakaoUserInfoDTO.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            log.error("Kakao 사용자 정보 조회 API 호출 실패: 상태 코드 {}, 응답 본문 {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(CommonErrorCode.BAD_REQUEST);
        }
    }

    // 카카오 요청 엔티티 생성
    private HttpEntity<MultiValueMap<String, String>> createKakaoRequestEntity(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return new HttpEntity<>(headers);
    }
}