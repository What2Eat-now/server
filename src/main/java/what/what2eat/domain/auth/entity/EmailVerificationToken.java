package what.what2eat.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Table(name = "email")
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "token", nullable = false)
    private String token;

    @Builder.Default
    @Column(name = "email_status", nullable = false)
    private boolean emailStatus = false;

    public void changeStatus() {
        this.emailStatus = true;
    }
}
