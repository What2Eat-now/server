package what.what2eat.domain.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import what.what2eat.domain.restaurant.controller.dto.RestaurantRequestDTO;
import what.what2eat.domain.restaurant.service.RestaurantService;
import what.what2eat.domain.restaurant.controller.dto.RestaurantResponseDTO;
import what.what2eat.global.response.ApiResponse;

import java.net.URISyntaxException;

@Controller
@Slf4j
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "음식점 관련 컨트롤러", description = "주변 음식점 조회, 랜덤 음식점 추천 API")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("")
    @Operation(summary = "주변 음식점 조회")

    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantApiResultDTO>> getAllRestaurants(
                                                                               @RequestParam Double latitude,
                                                                               @RequestParam Double longitude,
                                                                               @RequestParam(required = false, defaultValue = "1000") Integer distance) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(
                        restaurantService.getNearbyRestaurants(
                                RestaurantRequestDTO.RestaurantGetDTO.builder()
                                        .latitude(latitude)
                                        .longitude(longitude)
                                        .distance(distance)
                                        .build())));
    }

    @GetMapping("/random")
    @Operation(summary = "랜덤 음식점 조회", description = "주변 음식점 중에서 랜덤으로 하나를 지정해서 반환")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantInfoDTO>> getRandomRestaurant(
                                                                                @RequestParam Double latitude,
                                                                                @RequestParam Double longitude,
                                                                                @RequestParam(required = false, defaultValue = "1000") Integer distance) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(restaurantService.getRandomRestaurant(
                        RestaurantRequestDTO.RestaurantGetDTO.builder()
                                .latitude(latitude)
                                .longitude(longitude)
                                .distance(distance)
                                .build())));
    }
}
