package what.what2eat.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    SUCCESS("200", "요청이 성공적으로 처리되었습니다."),
    CREATED("201", "리소스가 성공적으로 생성되었습니다."),
    CONFIRM("202", "검증 및 확인되었습니다."),
    NEED_SIGNUP("307", "회원가입 화면으로 이동합니다."),
    NEED_UPDATE_NICKNAME("307", "닉네임 설정 화면으로 이동합니다.");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
