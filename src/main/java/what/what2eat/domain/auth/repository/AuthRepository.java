package what.what2eat.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String email);

    Boolean existsByUserEmail(String email);
}
