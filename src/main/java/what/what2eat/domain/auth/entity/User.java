package what.what2eat.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.diary.entity.Diary;
import what.what2eat.domain.usergroup.entity.UserGroup;
import what.what2eat.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", nullable = false, length = 50)
    private String userEmail;

    @Column(name = "nick_name", nullable = false, length = 20)
    private String nickName;

    @Column(name = "user_img", length = 100)
    private String userImg;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Diary> diaries = new ArrayList<>();

    // 그룹 할당 (명시적 양방향 관계 설정)
    public void assignGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    // 그룹 삭제 (명시적 양방향 관계 설정)
    public void removeGroup() {
        this.userGroup = null;
    }

    public void delete() {
        this.userStatus = UserStatus.DELETED;
    }
}