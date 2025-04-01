package haru.harudrawer.domain.auth.service.social;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.controller.dto.response.SocialResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.Role;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.service.CommonAuthService;
import haru.harudrawer.domain.auth.service.TokenService;
import haru.harudrawer.global.exception.CommonErrorCode;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.jwt.JwtProvider;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Service
@Slf4j
@Transactional
public class KakaoAuthService extends AbstractSocialAuthService{

    public KakaoAuthService(AuthRepository authRepository, TokenService tokenService, CommonAuthService commonAuthService, AuthConverter authConverter, RedisService redisService, RestTemplate restTemplate, JwtProvider jwtProvider) {
        super(authRepository, tokenService, commonAuthService, authConverter, redisService, restTemplate, jwtProvider);
    }

    @Override
    protected SocialRequestDTO.SocialUserInfoDTO getSocialUserInfo(String tokenOrCode) throws Exception {
        // 사용자 정보 조회
        SocialResponseDTO.KakaoUserInfoDTO userInfo = getKakaoUserInfo(tokenOrCode);

        return SocialRequestDTO.SocialUserInfoDTO.builder()
                .userEmail(userInfo.getKakaoAccount().getKakaoEmail())
                .nickName(userInfo.getKakaoAccount().getProfile().getKakaoNickName())
                .build();
    }

    @Override
    protected Provider getProvider() {
        return Provider.KAKAO;
    }

    /**
     * 카카오 회원 탈퇴
     */
    public void delete() {
        // 카카오 연결 해제
//        unlinkKakaoAccount(kakaoAccessToken);

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