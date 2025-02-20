package what.what2eat.domain.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import what.what2eat.domain.auth.entity.Provider;

public class CommonResponseDTO {


    @Getter
    @Builder
    public static class GetUserInfoDTO {
        private String userEmail;

        private String nickName;

        private Provider provider;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponseDTO {

        private boolean requiresSignup;

        private String email;

        private TokenDTO tokens;
    }


    @Getter
    @Builder
    public static class TokenDTO {
        private String accessToken;
        private String refreshToken;
    }
}
