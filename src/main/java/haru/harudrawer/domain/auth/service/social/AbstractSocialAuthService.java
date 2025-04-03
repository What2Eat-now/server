package haru.harudrawer.domain.auth.service.social;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.service.CommonAuthService;
import haru.harudrawer.domain.auth.service.TokenService;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractSocialAuthService implements SocialAuthService{

    protected final AuthRepository authRepository;
    protected final TokenService tokenService;
    protected final CommonAuthService commonAuthService;
    protected final AuthConverter authConverter;
    protected final RedisService redisService;
    protected final RestTemplate restTemplate;
    protected final JwtProvider jwtProvider;

    @Override
    public final CommonResponseDTO.LoginResponseDTO login(String tokenOrCode) throws Exception {
        // Provider 기준으로 사용자 정보 조회
        SocialRequestDTO.SocialUserInfoDTO userInfo = getSocialUserInfo(tokenOrCode);

        // 사용자 존재 여부 확인
        Optional<User> userOpt = authRepository.findByUserEmail(userInfo.getUserEmail());

        // 사용자가 존재할 경우
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();

            // 다른 Provider로 가입된 경우 예외 처리
            if (!existingUser.getProvider().equals(getProvider())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL);
            }

            // 존재하지 사용자일 경우 기존 응답 반환
            return createLoginResponse(existingUser);
        }

        // 사용자가 존재하지 않을 경우 회원가입 처리
        User newUser = signup(userInfo);
        return createLoginResponse(newUser);
    }

    protected abstract SocialRequestDTO.SocialUserInfoDTO getSocialUserInfo(String tokenOrCode) throws Exception;

    protected abstract Provider getProvider();

    protected CommonResponseDTO.LoginResponseDTO createLoginResponse(User user) {
        CommonResponseDTO.TokenDTO tokens = tokenService.createTokens(user);

        return CommonResponseDTO.LoginResponseDTO.builder()
                .userEmail(user.getUserEmail())
                .tokens(tokens)
                .build();
    }

    protected User signup(SocialRequestDTO.SocialUserInfoDTO userInfo) {
        User user = authConverter.userEmailToSocialUserEntity(userInfo, getProvider());
        authRepository.save(user);
        return user;
    }

}
