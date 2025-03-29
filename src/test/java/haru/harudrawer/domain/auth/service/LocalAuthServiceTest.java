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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private LocalAuthService localAuthService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtProvider jwtProvider;

    static String testEmail = "test@testing.com";
    static String testPassword = "Test0001!";
    static String testNickname = "Tester";
    static String testPhoneNumber = "01000030003";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void setupEmailVerification() {

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
    @Commit
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

        // 로그인 객체 생성
        LocalRequestDTO.LoginRequestDTO loginRequestDTO = LocalRequestDTO.LoginRequestDTO.builder()
                .userEmail(testEmail)
                .password(testPassword).build();


        // 토큰 생성 및 검증
        String accessToken = "dummyAccessToken";
        String refreshToken = "dummyRefreshToken";

        //  메소드를 실행하면 설정한 결과를 반환하도록 설정
        when(jwtProvider.createAccessToken(any(CustomUserDetails.class))).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(anyString())).thenReturn(refreshToken);

        //When

        // 로그인 진행
        CommonResponseDTO.LoginResponseDTO response = localAuthService.login(loginRequestDTO);

        // 사용자 조회
        Optional<User> userOpt= authRepository.findByUserEmail(testEmail);

        //Then
        assertNotNull(response.getTokens());
        assertEquals(accessToken, response.getTokens().getAccessToken());
        assertEquals(refreshToken, response.getTokens().getRefreshToken());

        assertTrue(userOpt.isPresent(), "사용자를 찾을 수 없습니다.");
        assertEquals(userOpt.get().getNickName(), testNickname);

        verify(redisService).saveToken(testEmail, refreshToken, Provider.LOCAL, TokenType.SERVER);
    }
}