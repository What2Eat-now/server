package haru.harudrawer.domain.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import haru.harudrawer.domain.auth.entity.Provider;

public class CommonResponseDTO {


    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GetUserInfoDTO {
        private String userEmail;

        private String nickName;

        private String userName;

        private String phoneNumber;

        private Provider provider;

    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponseDTO {

        private String userEmail;

        private TokenDTO tokens;
    }


    @Getter
    @Builder
    public static class TokenDTO {
        private String accessToken;
        private String refreshToken;
    }
}
