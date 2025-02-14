package what.what2eat.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    // 클라이언트로 전송할 발신 이메일
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 인증 코드 메일 전송
    public String sendVerificationEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(toEmail); // 목적지
        helper.setSubject("이메일 인증 코드 안내"); // 타이틀
        helper.setFrom(fromEmail); // 발신 이메일

        // HTML 템플릿에서 인증 코드를 치환
        String htmlContent = loadHtmlTemplate();
        String token = String.valueOf(createNumber());

        helper.setText(htmlContent.replace("${verificationCode}", token), true);

        mailSender.send(message);

        return token;
    }

    public int createNumber() {
        return (int) ((Math.random() * (90000)) + 100000);
    }

    private String loadHtmlTemplate() {

        return "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>이메일 인증 코드</title>\n" +
                "  <style>\n" +
                "    body { font-family: 'Arial', sans-serif; background-color: #f2f2f2; margin: 0; padding: 0; }\n" +
                "    .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center; }\n" +
                "    .header { margin-bottom: 30px; }\n" +
                "    .header h1 { font-size: 24px; color: #333333; }\n" +
                "    .content { font-size: 16px; color: #555555; margin-bottom: 30px; line-height: 1.5; }\n" +
                "    .verification-code { font-size: 32px; font-weight: bold; letter-spacing: 2px; color: #007BFF; margin: 20px 0; }\n" +
                "    .footer { font-size: 12px; color: #999999; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"email-container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>이메일 인증</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <p>아래 인증 코드를 복사하여 회원가입/로그인 화면에 입력해 주세요.</p>\n" +
                "      <div class=\"verification-code\">\n" +
                "        ${verificationCode}\n" +
                "      </div>\n" +
                "      <p>이 코드는 10분 동안 유효합니다.</p>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "      <p>© 2025 YourCompany. All rights reserved.</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
