package what.what2eat.domain.location.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.meeting.entity.Meeting;
import what.what2eat.global.common.entity.BaseEntity;

@Entity
@Table(name = "locationTracking")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationTracking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @Column(name = "latitude", nullable = false, length = 100)
    private String latitude;

    @Column(name = "longitude", nullable = false, length = 100)
    private String longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
}

