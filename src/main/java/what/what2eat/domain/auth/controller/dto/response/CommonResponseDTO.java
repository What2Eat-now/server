package what.what2eat.domain.auth.controller.dto.response;

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
}
