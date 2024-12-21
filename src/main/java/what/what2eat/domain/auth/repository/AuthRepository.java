package what.what2eat.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import what.what2eat.domain.auth.entity.User;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    User findByUserEmail(String email);
}
