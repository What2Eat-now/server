package what.what2eat.domain.auth.local.controller.dto;

import lombok.Builder;
import lombok.Getter;

public class LocalAuthRequestDTO {


    @Getter
    @Builder
    public static class SignUpRequestDTO{
        private String username;

        private String password;

        private String nickName;

        private String userImgUrl;

    }

    @Getter
    @Builder
    public static class LoginRequestDTO{
        private String username;

        private String password;
    }
}
