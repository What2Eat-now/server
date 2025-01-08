package what.what2eat.domain.auth.controller.dto;

import lombok.Builder;
import lombok.Getter;

public class KakaoAuthRequestDTO {

    @Builder
    @Getter
    public static class KakaoSignupDTO {
        private String userEmail;

        private String nickName;

        private Long socialId;
    }
}
