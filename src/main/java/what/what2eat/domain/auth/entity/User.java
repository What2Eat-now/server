package what.what2eat.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.usergroup.entity.UserGroup;
import what.what2eat.domain.location.entity.LocationTracking;
import what.what2eat.domain.meeting.entity.Participant;
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

    @Column(name = "user_email", unique = true, nullable = false, length = 20)
    private String userEmail;

    @Column(name = "nick_name", nullable = false, length = 20)
    private String nickName;

    @Column(name = "user_img", length = 100)
    private String userImg;

    @Column(name = "password", length = 40)
    private String password;

    @Column(name = "social_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Column(name = "social_id", length = 20)
    private Long socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    @OneToMany(mappedBy = "user")
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<LocationTracking> locationTrackings = new ArrayList<>();
}
