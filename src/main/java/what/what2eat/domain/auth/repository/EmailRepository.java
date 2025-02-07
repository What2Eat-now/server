package what.what2eat.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import what.what2eat.domain.auth.entity.EmailVerificationCode;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByUserEmailAndVerificationCodeAndEmailStatus(String userEmail, String code, Boolean status);

    Optional<EmailVerificationCode> findByUserEmailAndEmailStatus(String userEmail, Boolean status);

    Boolean existsByVerificationCode(String verificationCode);

}
