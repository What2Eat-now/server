package what.what2eat.domain.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import what.what2eat.domain.store.entity.Store;
import what.what2eat.global.common.entity.BaseEntity;

@Entity
@Table(name = "review")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "review_grade", nullable = false, length = 10)
    private Double reviewGrade;

    @Column(name = "review_content", nullable = false, length = 200)
    private String reviewContent;

    @Column(name = "review_img_url", length = 100)
    private String reviewImgUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}
