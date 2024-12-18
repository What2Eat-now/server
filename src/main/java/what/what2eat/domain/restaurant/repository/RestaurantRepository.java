package what.what2eat.domain.restaurant.repository;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import what.what2eat.domain.restaurant.entity.Restaurant;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    //위치 기반 데이터 조회
    @Query(value = "select CS from Restaurant as CS where ST_CONTAINS(ST_BUFFER(:point, :distance), CS.restaurantLocation)")
    List<Restaurant> findByLocationBased(Point point, Integer distance);


    // API 호출시 반환받는 데이터에 포함된 가게 아이디 전체 조회
    @Query(value = "select s.restaurantApiId from Restaurant s")
    List<String> findAllrestaurantApiIdList();
}
