package what.what2eat.domain.store.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;


public class StoreRequestDTO {

    @Getter
    @Builder
    public static class StoreGetDTO {

        @NotBlank
        private String latitude;

        @NotBlank
        private String longitude;

    }
}
