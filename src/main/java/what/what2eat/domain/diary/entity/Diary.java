package what.what2eat.domain.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import what.what2eat.domain.auth.entity.User;

import java.time.LocalDate;

@Entity
@Table(name = "diary")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "content", nullable = false, length = 200)
    private String content;

    @Column(name = "place_name", nullable = false, length = 50)
    private String placeName;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "location", nullable = false, length = 50)
    private Point location;

    @Column(name = "marker_number", nullable = false)
    private Integer markerNumber;

    @Column(name = "rate", nullable = false, length = 10)
    private String rate;

    @Column(name = "upload_img", nullable = false, length = 100)
    private String uploadImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
