package haru.harudrawer.domain.auth.service.social;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.TokenType;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.service.CommonAuthService;
import haru.harudrawer.domain.auth.service.TokenService;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class KakaoAuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private CommonAuthService commonAuthService;

    @Mock
    private AuthConverter authConverter;

    @Mock
    private RedisService redisService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtProvider jwtProvider;

    private KakaoAuthService kakaoAuthService;

    @BeforeEach
    public void setup() {
        kakaoAuthService = new KakaoAuthService(authRepository, tokenService, commonAuthService, authConverter,
                redisService, restTemplate, jwtProvider){

        @Override
        protected SocialRequestDTO.SocialUserInfoDTO getSocialUserInfo(String tokenOrCode) {
            redisService.saveToken("test@kakao.com", "dummyToeken", Provider.KAKAO, TokenType.REFRESH);

            return SocialRequestDTO.SocialUserInfoDTO.builder()
                    .userEmail("test@kakao.com")
                    .nickName("tester")
                    .build();
        }
        };
    }

    @Test
    public void testLogin_existingUser() throws Exception {
        //Given
        User kakaoUser = User.builder()
                .userEmail("test@kakao.com")
                .nickName("tester")
                .provider(Provider.KAKAO)
                .build();

        CommonResponseDTO.TokenDTO tokens = CommonResponseDTO.TokenDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authRepository.findByUserEmail("test@kakao.com")).thenReturn(Optional.of(kakaoUser));
        when(tokenService.createTokens(kakaoUser)).thenReturn(tokens);


        //When
        CommonResponseDTO.LoginResponseDTO response = kakaoAuthService.login("dummy code");

        //Then
        assertNotNull(response);
        assertEquals(response.getUserEmail(), kakaoUser.getUserEmail());

        assertEquals(response.getTokens(), tokens);
        assertEquals("access", tokens.getAccessToken());
        assertEquals("refresh", tokens.getRefreshToken());

        verify(authRepository).findByUserEmail(kakaoUser.getUserEmail());
        verify(tokenService).createTokens(kakaoUser);
        verify(redisService).saveToken(eq(kakaoUser.getUserEmail()), anyString(), eq(Provider.KAKAO), eq(TokenType.REFRESH));
    }


    @Test
    public void testKakaoLogin_notExistingUser() throws Exception {
        //Given
        User newUser = User.builder()
                .userEmail("test@kakao.com")
                .nickName("tester")
                .provider(Provider.KAKAO)
                .build();

        CommonResponseDTO.TokenDTO tokens = CommonResponseDTO.TokenDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authRepository.findByUserEmail("test@kakao.com")).thenReturn(Optional.empty());
        when(authConverter.userEmailToSocialUserEntity(any(), eq(Provider.KAKAO))).thenReturn(newUser);
        when(tokenService.createTokens(newUser)).thenReturn(tokens);
        when(authRepository.save(any(User.class))).thenReturn(newUser);

        //When
        CommonResponseDTO.LoginResponseDTO response = kakaoAuthService.login("dummyCode");

        //Then
        assertNotNull(response);

        assertEquals(response.getUserEmail(), newUser.getUserEmail());
        assertEquals(response.getTokens(), tokens);

        verify(authRepository).findByUserEmail("test@kakao.com");
        verify(authRepository).save(any(User.class));
        verify(tokenService).createTokens(newUser);
        verify(redisService).saveToken(eq(newUser.getUserEmail()),"dummyToken", eq(Provider.KAKAO), eq(TokenType.REFRESH));
    }

    @Test
    public void testLogin_userExistDifferentProvider() {
        //Given
        User appleUser = User.builder()
                .userEmail("test@kakao.com")
                .nickName("tester")
                .provider(Provider.APPLE)
                .build();

        when(authRepository.findByUserEmail("test@kakao.com")).thenReturn(Optional.of(appleUser));

        //When
        assertThrows(AuthException.class, () -> kakaoAuthService.login("dummyToken"));

        //Then
        verify(authRepository).findByUserEmail("test@kakao.com");
        verifyNoInteractions(tokenService);
        verifyNoInteractions(authConverter);
    }
}