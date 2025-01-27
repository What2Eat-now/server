package what.what2eat.domain.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class CommonAuthRequestDTO {

    @Getter
    @Builder
    public static class UpdateInfoDTO {

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
        private String userEmail;

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
        private String nickName;
    }
}
