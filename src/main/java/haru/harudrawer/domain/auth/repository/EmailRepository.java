package haru.harudrawer.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import haru.harudrawer.domain.auth.entity.EmailVerificationCode;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByUserEmailAndVerificationCodeAndEmailStatus(String userEmail, String code, Boolean status);

    Optional<EmailVerificationCode> findByUserEmailAndEmailStatus(String userEmail, Boolean status);

    Optional<EmailVerificationCode> findByUserEmail(String userEmail);

    Boolean existsByVerificationCode(String verificationCode);



}
