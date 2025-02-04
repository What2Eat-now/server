package what.what2eat.domain.auth.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

public class CommonResponseDTO {


    @Getter
    @Builder
    public static class GetUserInfoDTO {
        private String userEmail;

        private String nickName;

    }
}
