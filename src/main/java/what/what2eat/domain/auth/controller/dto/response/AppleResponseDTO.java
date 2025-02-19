package what.what2eat.domain.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

public class AppleResponseDTO {


    @Getter
    @Builder
    public static class AppleTokenInfoDTO{

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private int expiresIn;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;
    }

    @Getter
    @Builder
    public static class AppleLoginResponseDTO {

        private boolean requireSignup;

        private String appleEmail;

        private String accessToken;
        private String refreshToken;


    }
}
