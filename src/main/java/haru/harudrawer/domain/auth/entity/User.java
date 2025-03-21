package haru.harudrawer.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import haru.harudrawer.domain.diary.entity.Diary;
import haru.harudrawer.domain.usergroup.entity.UserGroup;
import haru.harudrawer.global.common.entity.BaseEntity;

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

    @Column(name = "phone_number", length = 25)
    private String phoneNumber;

    @Lob
    @Column(name = "phone_hash", unique = true, columnDefinition = "TEXT")
    private String phoneHash;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

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

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }
    public void updateEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public void updatePassword(String password) {
        this.password = password;
    }


}