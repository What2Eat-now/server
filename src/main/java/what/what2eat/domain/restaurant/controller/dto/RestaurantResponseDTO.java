package what.what2eat.domain.restaurant.controller.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RestaurantResponseDTO {

    @Getter
    @Setter
    @Builder
    public static class RestaurantApiResultDTO{
        @JsonProperty("documents")
        private List<RestaurantInfoDTO> documents;

        @JsonProperty("meta")
        private MetaDTO meta;

    }

    @Getter
    @Setter
    @Builder
    public static class RestaurantInfoDTO {


        @NotBlank
        @JsonProperty("address_name")
        private String address;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("id")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String restaurantId;

        @JsonProperty("phone")
        private String restaurantPhoneNumber;

        @JsonProperty("place_name")
        private String restaurantName;

        @JsonProperty("place_url")
        private String restaurantUrl;

        @JsonProperty("x")
        private Double longitude;

        @JsonProperty("y")
        private Double latitude;
    }

    @Getter
    @Setter
    public static class MetaDTO {

        @JsonProperty("is_end")
        private boolean isEnd;

        @JsonProperty("pageable_count")
        private int pageableCount;

        @JsonProperty("total_count")
        private int totalCount;
    }
}