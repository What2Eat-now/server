package what.what2eat.domain.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantApiResultDTO>> getAllRestaurants(@Valid @RequestBody RestaurantRequestDTO.RestaurantGetDTO request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(restaurantService.getNearbyRestaurants(request)));
    }

    @GetMapping("/random")
    @Operation(summary = "랜덤 음식점 조회", description = "주변 음식점 중에서 랜덤으로 하나를 지정해서 반환")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantInfoDTO>> getRandomRestaurant(@RequestBody RestaurantRequestDTO.RestaurantGetDTO request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(restaurantService.getRandomRestaurant(request)));
    }
}
