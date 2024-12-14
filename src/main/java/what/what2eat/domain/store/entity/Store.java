package what.what2eat.domain.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import what.what2eat.domain.category.entity.Category;
import what.what2eat.domain.meeting.entity.Meeting;
import what.what2eat.domain.review.entity.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @Column(name = "store_api_id", nullable = false, unique = true)
    private String storeApiId;

    @Column(name = "store_name", nullable = false, length = 30)
    private String storeName;

    @Column(name = "category_group_name", nullable = false, length = 20)
    private String categoryGroupName;

    @Column(name = "category_name", nullable = false, length = 30)
    private String categoryName;

    @Column(name = "store_address", nullable = false, length = 50)
    private String storeAddress;

    @Column(name = "store_road_address", nullable = false, length = 50)
    private String storeRoadAddress;

    @Column(name = "store_number", nullable = false, length = 20)
    private String storeNumber;

    @Column(name = "location", nullable = false,  columnDefinition = "POINT SRID 4326")
    private Point location;

    @Column(name = "store_url", nullable = false, length = 50)
    private String storeUrl;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
