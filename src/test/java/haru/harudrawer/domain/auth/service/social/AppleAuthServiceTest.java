package haru.harudrawer.domain.auth.service.social;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.converter.AuthConverter;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.Role;
import haru.harudrawer.domain.auth.entity.TokenType;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.service.CommonAuthService;
import haru.harudrawer.domain.auth.service.TokenService;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.jwt.JwtProvider;
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
class AppleAuthServiceTest {

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

    private AppleAuthService appleAuthService;

    @BeforeEach
    public void setup() throws Exception {
        // 상속으로 인해 Spy, InjectMocks가 불가능
        // 따라서 서브 클래스를 생성해 getSocialUserInfo를 직접 오버라이딩해서 사용
        appleAuthService = new AppleAuthService(authRepository, tokenService, commonAuthService,
                authConverter, redisService, restTemplate, jwtProvider) {
            @Override
            protected SocialRequestDTO.SocialUserInfoDTO getSocialUserInfo(String tokenOrCode) {
                redisService.saveToken("test@apple.com", "dummyRefreshToken", Provider.APPLE, TokenType.REFRESH);

                return SocialRequestDTO.SocialUserInfoDTO.builder()
                        .userEmail("test@apple.com")
                        .build();
            }
        };
    }

    @Test
    public void signupTest() {
        //Given
        SocialRequestDTO.SocialUserInfoDTO dummyUserInfo = SocialRequestDTO.SocialUserInfoDTO.builder()
                .userEmail("test@apple.com")
                .build();

        User appleTester = User.builder()
                .userEmail("test@apple.com")
                .nickName("이름을 변경해주세요.")
                .role(Role.USER)
                .provider(Provider.APPLE)
                .build();

            // stub 생성
        when(authConverter.userEmailToSocialUserEntity(any(SocialRequestDTO.SocialUserInfoDTO.class), eq(Provider.APPLE)))
                .thenReturn(appleTester);

        when(authRepository.save(any(User.class))).thenReturn(appleTester);

        //When
        User result = appleAuthService.signup(dummyUserInfo);

        //Then

            // 가입된 사용자 검증
        assertNotNull(result);
        assertEquals("test@apple.com", result.getUserEmail());
        assertEquals("이름을 변경해주세요.", result.getNickName());
        assertEquals(Provider.APPLE, result.getProvider());

            // 메소드 호출 내역 검증
        verify(authConverter).userEmailToSocialUserEntity(dummyUserInfo, Provider.APPLE);
        verify(authRepository).save(appleTester);

    }

    @Test
    public void testLogin_existingUser() throws Exception{

        //Given
        User existingUser = User.builder()
                .userEmail("test@apple.com")
                .nickName("AppleTester")
                .provider(Provider.APPLE)
                .build();

        CommonResponseDTO.TokenDTO dummyTokens = CommonResponseDTO.TokenDTO.builder()
                .accessToken(null)
                .refreshToken(null)
                .build();

            // stub 생성
        when(authRepository.findByUserEmail("test@apple.com")).thenReturn(Optional.of(existingUser));
        when(tokenService.createTokens(existingUser)).thenReturn(dummyTokens);

        //When
            // 로그인 호출
        CommonResponseDTO.LoginResponseDTO response = appleAuthService.login("dummyToken");

        //Then

            // 로그인 결과 검증
        assertNotNull(response);
        assertEquals("test@apple.com", response.getUserEmail());
        assertEquals(dummyTokens, response.getTokens());

        verify(authRepository).findByUserEmail("test@apple.com");
        verify(redisService).saveToken(eq("test@apple.com"), anyString(), eq(Provider.APPLE), eq(TokenType.REFRESH));
    }

    @Test
    public void testLogin_notExistingUser() throws Exception {

        // flow
        // login -> email 조회 -> signup -> convert and save

        //Given

            // 실제 가입할 사용자
        User user = User.builder()
                .userEmail("thsgur1212@test.com")
                .nickName("son")
                .provider(Provider.APPLE)
                .build();

        CommonResponseDTO.TokenDTO tokens = CommonResponseDTO.TokenDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authRepository.findByUserEmail(any(String.class))).thenReturn(Optional.empty());
        when(authConverter.userEmailToSocialUserEntity(any(SocialRequestDTO.SocialUserInfoDTO.class), eq(Provider.APPLE))).thenReturn(user);
        when(authRepository.save(any(User.class))).thenReturn(user);

        when(tokenService.createTokens(user)).thenReturn(tokens);

        //When
        CommonResponseDTO.LoginResponseDTO response = appleAuthService.login("dummyCode");

        //Then
        assertNotNull(response);
        assertEquals(response.getUserEmail(), user.getUserEmail());
        assertEquals(response.getTokens(), tokens);

        verify(authRepository).findByUserEmail(user.getUserEmail()); // or anyString()
        verify(authConverter).userEmailToSocialUserEntity(any(), eq(Provider.APPLE));
        verify(authRepository).save(user);
        verify(tokenService).createTokens(user);
    }

    @Test
    public void testLogin_userExistDifferentProvider() {
        //Given
        User kakaoUser = User.builder()
                .userEmail("test@apple.com")
                .nickName("kakaoUser")
                .provider(Provider.KAKAO)
                .build();

        when(authRepository.findByUserEmail("test@apple.com")).thenReturn(Optional.of(kakaoUser));

        //When
        assertThrows(AuthException.class, () -> appleAuthService.login("dummyToken"));

        //Then
        verify(authRepository).findByUserEmail("test@apple.com");
        verifyNoInteractions(tokenService);
        verifyNoInteractions(authConverter);
    }
}