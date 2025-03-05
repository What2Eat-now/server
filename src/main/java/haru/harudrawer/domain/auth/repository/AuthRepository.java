package haru.harudrawer.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import haru.harudrawer.domain.auth.entity.User;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String email);


    Optional<User> findByUserId(Long userId);
}
