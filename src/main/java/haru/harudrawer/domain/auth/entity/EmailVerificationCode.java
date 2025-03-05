package haru.harudrawer.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Table(name = "email")
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Builder.Default
    @Column(name = "email_status", nullable = false)
    private boolean emailStatus = false;

    @Column(name = "expiryDate", nullable = false)
    private LocalDateTime expiryDate;

    public void changeStatus() {
        this.emailStatus = true;
    }
}
