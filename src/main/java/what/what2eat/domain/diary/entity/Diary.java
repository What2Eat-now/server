package what.what2eat.domain.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.web.multipart.MultipartFile;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.diary.controller.dto.DiaryRequestDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private Integer rate;

    @Builder.Default
    @OneToMany(mappedBy = "diary",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryImage> diaryImageList = new ArrayList<>(); // 이미지 데이터

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void update(DiaryRequestDTO.DiaryUpdateDTO request, Point location) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.placeName = request.getPlaceName();
        this.visitDate = request.getVisitDate();
        this.location = location;
        this.markerNumber = request.getMarkerNumber();
        this.rate = request.getRate();
    }

}
