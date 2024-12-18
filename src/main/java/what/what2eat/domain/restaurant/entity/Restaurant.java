package what.what2eat.domain.restaurant.entity;

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
@Table(name = "restaurant")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(name = "restaurant_api_id", nullable = false, unique = true)
    private String restaurantApiId;

    @Column(name = "restaurant_name", nullable = false, length = 30)
    private String restaurantName;

    @Column(name = "category_name", nullable = false, length = 30)
    private String categoryName;

    @Column(name = "restaurant_address", nullable = false, length = 50)
    private String restaurantAddress;

    @Column(name = "restaurant_number", nullable = false, length = 20)
    private String restaurantNumber;

    @Column(name = "restaurant_Location", nullable = false,  columnDefinition = "POINT SRID 4326")
    private Point restaurantLocation;

    @Column(name = "restaurant_url", nullable = false, length = 50)
    private String restaurantUrl;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
