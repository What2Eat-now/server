package what.what2eat.domain.usergroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.meeting.entity.Meeting;
import what.what2eat.global.common.entity.BaseEntity;

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

    @Column(name = "user_group_code", nullable = false, length = 50, unique = true)
    private String userGroupCode;

    @Builder.Default
    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

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

