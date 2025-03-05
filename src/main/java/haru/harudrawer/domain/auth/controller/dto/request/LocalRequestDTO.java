package haru.harudrawer.domain.auth.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class LocalRequestDTO {

    // 로컬
    @Getter
    @Builder
    public static class SignUpRequestDTO{

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
        private String userEmail;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        private String password;

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
        private String nickName;

        @NotBlank(message = "핸드폰 번호는 필수 입력 항목입니다.")
        @Size(max = 25, message = "핸드폰 번호는 최대 25자까지 가능합니다.")
        private String phoneNumber;

        private String userImgUrl;

    }

    @Getter
    @Builder
    public static class LoginRequestDTO{
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String userEmail;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        private String password;
    }

    @Getter
    @Builder
    public static class ResetPasswordDTO{
        @NotBlank(message = "사용자 이메일은 필수 입력 항목입니다.")
        private String userEmail;

        @NotBlank(message = "변경 비밀번호는 필수 입력 항목입니다.")
        private String newPassword;

        @NotBlank(message = "변경 확인 비밀번호는 필수 입력 항목입니다.")
        private String newPasswordCheck;
    }

    @Getter
    @Builder
    public static class VerifyCodeDTO {
        private String userEmail;
        private String code;

    }
}
