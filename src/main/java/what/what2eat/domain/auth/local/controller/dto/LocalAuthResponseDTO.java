package what.what2eat.domain.auth.local.controller.dto;

import lombok.Builder;
import lombok.Getter;

public class LocalAuthResponseDTO {
    @Getter
    @Builder
    public static class LoginResponseDTO{
        private String accessToken;
        private String refreshToken;
    }
}
