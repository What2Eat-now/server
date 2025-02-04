package what.what2eat.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import what.what2eat.domain.auth.entity.EmailVerificationToken;
import what.what2eat.domain.auth.entity.User;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByUserEmailAndTokenAndEmailStatus(String userEmail, String token, Boolean status);

    Optional<EmailVerificationToken> findByUserEmailAndEmailStatus(String userEmail,Boolean status);

}
