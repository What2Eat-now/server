package what.what2eat.global.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS("200", "요청이 성공적으로 처리되었습니다."),
    CREATED("201", "리소스가 성공적으로 생성되었습니다.");
    // 커스텀해서 추가 가능!

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
