package what.what2eat.domain.store.controller.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class StoreResponseDTO {

    @Getter
    @Setter
    @Builder
    public static class StoreApiResultDTO{
        @JsonProperty("documents")
        private List<StoreInfoDTO> documents;

        @JsonProperty("meta")
        private MetaDTO meta;

    }

    @Getter
    @Setter
    @Builder
    public static class StoreInfoDTO {

        @NotBlank
        @JsonProperty("address_name")
        private String address;

        @JsonProperty("category_group_name")
        private String categoryGroupName;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("distance")
        private String distance;

        @JsonProperty("id")
        private String storeId;

        @JsonProperty("phone")
        private String storePhoneNumber;

        @JsonProperty("place_name")
        private String storeName;

        @JsonProperty("place_url")
        private String storeUrl;

        @JsonProperty("road_address_name")
        private String storeRoadAddress;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;
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
