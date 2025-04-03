package haru.harudrawer.domain.auth.service;

import haru.harudrawer.domain.auth.controller.dto.request.LocalRequestDTO;
import haru.harudrawer.domain.auth.entity.EmailVerificationCode;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {
    private final JavaMailSender mailSender;

    // 클라이언트로 전송할 발신 이메일
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("classpath:templates/email-verification.html")
    private Resource emailTemplate;

    private final EmailRepository emailRepository;

    /**
     * 인증 코드 메일 전송
     */
    public String sendVerificationEmail(String toEmail) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(toEmail); // 목적지
        helper.setSubject("이메일 인증 코드 안내"); // 타이틀
        helper.setFrom(fromEmail); // 발신 이메일

        // HTML 템플릿에서 인증 코드를 치환
        String htmlContent = new String(emailTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String token = String.valueOf(generateVerificationCode());

        helper.setText(htmlContent.replace("${verificationCode}", token), true);

        mailSender.send(message);

        return token;
    }

    /**
     * 인증번호 이메일 전송
     */
    public void sendAndSaveEmail(String userEmail) throws MessagingException, IOException {

        Optional<EmailVerificationCode> existingVerificationCode = emailRepository.findByUserEmail(userEmail);

        // 이메일 전송 후 인증번호 반환
        String code = sendVerificationEmail(userEmail);

        if (existingVerificationCode.isPresent()) {
            EmailVerificationCode emailVerificationCode = existingVerificationCode.get();

            emailVerificationCode.updateCode(code);
        } else {
            // 이메일 정보 저장
            emailRepository.save(EmailVerificationCode.builder()
                    .userEmail(userEmail)
                    .emailStatus(false)
                    .verificationCode(code)
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .build());
        }
    }

    /**
     * 이메일 인증 상태 조회 및 삭제
     */
    public void checkEmailVerification(String userEmail) {
        // 인증번호 엔티티에서 이메일과 인증 상태로 조회
        EmailVerificationCode byUserEmail = emailRepository.findByUserEmailAndEmailStatus(userEmail, true)
                .orElseThrow(() -> new AuthException(AuthErrorCode.NEED_VERIFICATION));

        // 인증 상태가 false인 경우
        if (!byUserEmail.isEmailStatus()) {
            throw new AuthException(AuthErrorCode.NEED_VERIFICATION);
        }

        // 인증된 객체 삭제
        emailRepository.delete(byUserEmail);
    }


    /**
     * 인증번호 검증
     */
    public void verifyCode(LocalRequestDTO.VerifyCodeDTO request) {
        // 인증 토큰 검증
        if (Boolean.FALSE.equals(emailRepository.existsByVerificationCode(request.getCode()))) {
            throw new AuthException(AuthErrorCode.INVALID_CERTIFICATION_CODE);
        }

        // 인증 번호와 이메일로 저장된 정보 찾기
        EmailVerificationCode findCode = emailRepository.findByUserEmailAndVerificationCodeAndEmailStatus(request.getUserEmail(), request.getCode(), false)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 인증 코드 시간 만료된 경우
        if (findCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        // 인증 상태 변경
        findCode.changeStatus();
    }

    // 인증 코드 생성
    private int generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        return 100000 + secureRandom.nextInt(900000);
    }

}
