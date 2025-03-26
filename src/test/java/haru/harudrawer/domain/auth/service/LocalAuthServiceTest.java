package haru.harudrawer.domain.auth.service;

import haru.harudrawer.domain.auth.controller.dto.request.LocalRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.entity.EmailVerificationCode;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.TokenType;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.auth.repository.EmailRepository;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.security.domain.CustomUserDetails;
import haru.harudrawer.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class LocalAuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RedisService redisService;

    @Mock
    private LocalAuthService localAuthService;

    @Mock
    private JwtProvider jwtProvider;

    static String testEmail = "test@test.com";
    static String testPassword = "TestPassword!";
    static String testNickname = "Tester";
    static String testPhoneNumber = "01000000000";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void setupEmailVerification() {
        String testEmail = "test@test.com";

        emailRepository.findByUserEmail(testEmail).ifPresent(emailRepository::delete);

        EmailVerificationCode evc = EmailVerificationCode.builder()
                .userEmail(testEmail)
                .verificationCode("123456")
                .emailStatus(true)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        emailRepository.save(evc);
    }


    @Test
    @DisplayName("회원가입 테스트")
    public void signupTest() {
        //Given
        LocalRequestDTO.SignUpRequestDTO tester = LocalRequestDTO.SignUpRequestDTO.builder()
                .userEmail(testEmail)
                .password(testPassword)
                .nickName(testNickname)
                .phoneNumber(testPhoneNumber)
                .build();

        //When
        assertDoesNotThrow(() -> localAuthService.signUp(tester));

        //Then
        Optional<User> createdUser = authRepository.findByUserEmail(testEmail);
        assertTrue(createdUser.isPresent(), "회원가입 후 사용자가 DB에 저장되어야 합니다.");

        Optional<EmailVerificationCode> evcAfter = emailRepository.findByUserEmail(testEmail);
        assertTrue(evcAfter.isEmpty(), "회원가입 후 이메일 인증 정보는 삭제되어야 합니다.");
    }

    @Test
    @DisplayName("로그인 테스트")
    public void loginTest() throws Exception {
        //Given

        LocalRequestDTO.LoginRequestDTO loginRequestDTO = LocalRequestDTO.LoginRequestDTO.builder()
                .userEmail(testEmail)
                .password(testPassword).build();

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(1L)
                .email(testEmail)
                .password(testPassword)
                .provider(Provider.LOCAL)
                .nickName("손혁")
                .authorities(Collections.emptyList())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String accessToken = "dummyAccessToken";
        String refreshToken = "dummyRefreshToken";
        when(jwtProvider.createAccessToken(userDetails)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(testEmail)).thenReturn(refreshToken);

        //When
        CommonResponseDTO.LoginResponseDTO response = localAuthService.login(loginRequestDTO);


        //Then
        assertNull(response);
        assertNotNull(response.getTokens());

        assertEquals(accessToken, response.getTokens().getAccessToken());
        assertEquals(refreshToken, response.getTokens().getRefreshToken());

        verify(redisService).saveToken(testEmail, refreshToken, Provider.LOCAL, TokenType.SERVER);
    }
}