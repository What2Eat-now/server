package haru.harudrawer.domain.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

public class KakaoResponseDTO {

    @Getter
    @Builder
    public static class KakaoLoginResponseDTO {
        private boolean requiresSignup;
        private String kakaoEmail;
        private Map<String, String> tokens;
    }

    @Getter
    @Builder
    public static class KakaoTokenDTO{

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("expires_in")
        private String expires;

        @JsonProperty("refresh_token_expires_in")
        private String refreshTokenExpires;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KakaoUserInfoDTO {

        @JsonProperty("id")
        private Long userId;

        @JsonProperty("has_signed_up")
        private Boolean hasSignedUp;

        //서비스에 연결 완료된 시각. UTC
        @JsonProperty("connected_at")
        private Date connectedAt;

        //카카오싱크 간편가입을 통해 로그인한 시각. UTC
        @JsonProperty("synched_at")
        private Date synchedAt;

        //사용자 프로퍼티
        @JsonProperty("properties")
        private Map<String, String> properties;

        //uuid 등 추가 정보
        @JsonProperty("for_partner")
        private KakaoResponseDTO.Partner partner;

        //사용자 이메일 정보
        @JsonProperty("kakao_account")
        private KakaoResponseDTO.KakaoAccount kakaoAccount;
    }

    @Getter
    @Builder
    public static class KakaoAccount {

        @JsonProperty("email")
        private String kakaoEmail;
    }


    @Getter
    @Builder
    public static class Partner {
        //고유 ID
        @JsonProperty("uuid")
        private String uuid;
    }

}
