package what.what2eat.domain.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.auth.entity.User;

import java.time.LocalDateTime;

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

    @Column(name = "visit_place", nullable = false, length = 50)
    private String visit_place;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visit_date;

    @Column(name = "visit_location", nullable = false, length = 50)
    private String visit_location;

    @Column(name = "emotion", nullable = false)
    private String emotion;

    @Column(name = "rate", nullable = false, length = 10)
    private String rate;

    @Column(name = "img_url", nullable = false, length = 100)
    private String img_url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private User user;
}
