package what.what2eat.domain.restaurant.converter;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;
import what.what2eat.domain.restaurant.entity.Restaurant;
import what.what2eat.domain.restaurant.controller.dto.RestaurantResponseDTO;

@Component
@RequiredArgsConstructor
public class RestaurantConverter {

    private final GeometryFactory geometryFactory;

    public Restaurant toRestaurantEntity(RestaurantResponseDTO.RestaurantInfoDTO restaurantInfoDTO) {

        return Restaurant.builder()
                .restaurantApiId(restaurantInfoDTO.getRestaurantId())
                .categoryName(restaurantInfoDTO.getCategoryName())
                .restaurantAddress(restaurantInfoDTO.getAddress())
                .restaurantLocation(geometryFactory.createPoint(new Coordinate(restaurantInfoDTO.getLongitude(), restaurantInfoDTO.getLatitude())))
                .restaurantName(restaurantInfoDTO.getRestaurantName())
                .restaurantUrl(restaurantInfoDTO.getRestaurantUrl())
                .restaurantNumber(restaurantInfoDTO.getRestaurantPhoneNumber())
                .build();
    }

    public RestaurantResponseDTO.RestaurantInfoDTO toDTO(Restaurant restaurant) {
        return RestaurantResponseDTO.RestaurantInfoDTO.builder()
                .address(restaurant.getRestaurantAddress())
                .categoryName(restaurant.getCategoryName())
                .longitude(restaurant.getRestaurantLocation().getX())
                .latitude(restaurant.getRestaurantLocation().getY())
                .restaurantName(restaurant.getRestaurantName())
                .restaurantPhoneNumber(restaurant.getRestaurantNumber())
                .restaurantUrl(restaurant.getRestaurantUrl())
                .build();
    }
}
