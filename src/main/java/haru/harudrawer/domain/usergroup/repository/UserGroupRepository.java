package haru.harudrawer.domain.usergroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import haru.harudrawer.domain.usergroup.entity.UserGroup;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    boolean existsByUserGroupName(String groupName);

}
