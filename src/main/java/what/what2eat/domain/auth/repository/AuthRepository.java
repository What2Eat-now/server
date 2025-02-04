package what.what2eat.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmailAndUserStatus(String email, UserStatus userStatus);

    Boolean existsByUserEmailAndUserStatus(String email, UserStatus status);

    Optional<User> findByUserIdAndUserStatus(Long userId, UserStatus status);
}
