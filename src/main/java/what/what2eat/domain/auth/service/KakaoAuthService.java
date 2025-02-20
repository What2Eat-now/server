package what.what2eat.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import what.what2eat.domain.auth.controller.dto.request.KakaoRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.CommonResponseDTO;
import what.what2eat.domain.auth.controller.dto.response.KakaoResponseDTO;
import what.what2eat.domain.auth.converter.AuthConverter;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.AuthException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.exception.CommonErrorCode;
import what.what2eat.global.security.domain.CustomUserDetails;
import what.what2eat.global.security.jwt.JwtProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KakaoAuthService {

    private final RestTemplate restTemplate;
    private final AuthConverter authConverter;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;

    // 회원가입
    public void signup(KakaoRequestDTO.KakaoSignupDTO request) {
        //객체 변환후 저장
        authRepository.save(authConverter.signupToKakaoUserEntity(request));

    }

    // 토큰으로 사용자 정보 조회
    public CommonResponseDTO.LoginResponseDTO login(String kakaoAccessToken) {

        // 사용자 정보 조회
        KakaoResponseDTO.KakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoAccessToken);

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
                    .email(userInfo.getKakaoAccount().getKakaoEmail())
                    .tokens(null)
                    .build();
        }

        // 로그인 성공
        User user = userOpt.get();

        CommonResponseDTO.TokenDTO tokens = createTokens(user);

        return CommonResponseDTO.LoginResponseDTO.builder()
                .requiresSignup(false)
                .email(null)
                .tokens(tokens)
                .build();
    }

    /**
     * AccessToken 및 RefreshToken 생성
     */
    private CommonResponseDTO.TokenDTO createTokens(User user) {

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .password(null)
                .nickName(user.getNickName())
                .provider(user.getProvider())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().name())))
                .build();

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(user.getUserEmail());

        return CommonResponseDTO.TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    // 카카오 사용자 정보 조회
    private KakaoResponseDTO.KakaoUserInfoDTO getKakaoUserInfo(String kakaoAccessToken) {
        try {
            return restTemplate.postForEntity(
                    "https://kapi.kakao.com/v2/user/me",
                    createKakaoRequestEntity(kakaoAccessToken),
                    KakaoResponseDTO.KakaoUserInfoDTO.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            log.error("Kakao API 호출 실패: 상태 코드 {}, 응답 본문 {}", e.getStatusCode(), e.getResponseBodyAsString());
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