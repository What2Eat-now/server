package what.what2eat.domain.restaurant.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;


public class RestaurantRequestDTO {

    @Getter
    @Builder
    public static class RestaurantGetDTO {

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        private Integer distance;
    }
}