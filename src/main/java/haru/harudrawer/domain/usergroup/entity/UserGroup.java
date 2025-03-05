package haru.harudrawer.domain.usergroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "userGroup")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_group_id", nullable = false)
    private Long userGroupId;

    @Column(name = "user_group_name", nullable = false, length = 20)
    private String userGroupName;

    @Column(name = "user_group_password", nullable = false, length = 100)
    private String userGroupPassword;

    @Column(name = "user_group_owner", nullable = false)
    private String owner;

    @Builder.Default
    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

//    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Meeting> meetings = new ArrayList<>();

    // 그룹 구성원 추가 (양방향 관계 연결)
    public void addUser(User user) {
        users.add(user);
        user.assignGroup(this);
    }

    //그룹 구성원 삭제
    public void removeUser(User user) {
        users.remove(user);
        user.removeGroup();
    }
}

