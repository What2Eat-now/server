package haru.harudrawer.domain.auth.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

public class LocalResponseDTO {

    @Getter
    @Builder
    public static class LocalLoginResponseDTO{
        private String accessToken;
        private String refreshToken;
    }

}
