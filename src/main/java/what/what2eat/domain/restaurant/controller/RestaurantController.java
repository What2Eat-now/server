package what.what2eat.domain.restaurant.controller;

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
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantApiResultDTO>> getAllRestaurants(@Valid @RequestBody RestaurantRequestDTO.RestaurantGetDTO request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(restaurantService.getNearbyRestaurants(request)));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO.RestaurantInfoDTO>> getRandomRestaurant(@RequestBody RestaurantRequestDTO.RestaurantGetDTO request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(restaurantService.getRandomRestaurant(request)));
    }
}
