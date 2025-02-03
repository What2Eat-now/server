import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import what.what2eat.domain.auth.controller.dto.AuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
import what.what2eat.domain.auth.converter.KakaoAuthConverter;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;
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
    private final KakaoAuthConverter kakaoAuthConverter;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;


    // 회원가입
    public void signup(AuthRequestDTO.KakaoSignupDTO request) {
        //객체 변환후 저장
        authRepository.save(kakaoAuthConverter.signupToUserEntity(request));

    }

    // 토큰으로 사용자 정보 조회

    public AuthResponseDTO.KakaoLoginResponseDTO login(String kakaoAccessToken) {

        // 사용자 정보 조회
        AuthResponseDTO.KakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoAccessToken);

        // 사용자 존재 유무 확인
        Optional<User> userOpt = findUserByEmail(userInfo.getKakaoAccount().getKakaoEmail());

        if (userOpt.isEmpty()) {
            // 회원가입 필요 리다이렉트 처리
            return new AuthResponseDTO.KakaoLoginResponseDTO(
                    true, userInfo.getKakaoAccount().getKakaoEmail(), null);
        }

        // 로그인 성공
        User user = userOpt.get();
        Map<String, String> tokens = createTokens(user);

        return new AuthResponseDTO.KakaoLoginResponseDTO(
                false, null, tokens);
    }

    /**
     * AccessToken 및 RefreshToken 생성
     */
    private Map<String, String> createTokens(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getUserId(),
                user.getUserEmail(),
                null,
                user.getNickName(),
                user.getProvider(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(user.getUserEmail());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    // DB 조회
    private Optional<User> findUserByEmail(String email) {
        return authRepository.findByUserEmailAndUserStatus(email, UserStatus.ACTIVE);
    }

    // 카카오 사용자 정보 조회
    private AuthResponseDTO.KakaoUserInfoDTO getKakaoUserInfo(String kakaoAccessToken) {
        try {
            return restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    createKakaoRequestEntity(kakaoAccessToken),
                    AuthResponseDTO.KakaoUserInfoDTO.class
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
