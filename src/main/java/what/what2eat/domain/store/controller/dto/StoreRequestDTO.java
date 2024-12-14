package what.what2eat.domain.store.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;


public class StoreRequestDTO {

    @Getter
    @Builder
    public static class StoreGetDTO {

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        private Integer distance;
    }
}
