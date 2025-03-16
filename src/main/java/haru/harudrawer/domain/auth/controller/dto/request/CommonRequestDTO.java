package haru.harudrawer.domain.auth.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class CommonRequestDTO {

    @Getter
    @Builder
    public static class UpdateEmailDTO {

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
        private String userEmail;
    }

    @Getter
    @Builder
    public static class UpdateNickNameDTO {

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        private String nickName;
    }

    @Getter
    @Builder
    public static class UpdatePasswordDTO {

        @NotBlank(message = "기존 비밀번호는 필수 입력 항목입니다.")
        private String currentPassword;

        @NotBlank(message = "변경 비밀번호는 필수 입력 항목입니다.")
        private String newPassword;

    }

    @Getter
    @Builder
    public static class TokenRefreshDTO {

        @NotBlank(message = "refreshToken은 필수 입력 항목입니다.")
        private String refreshToken;
    }
}
