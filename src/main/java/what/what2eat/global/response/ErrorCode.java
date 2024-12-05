package what.what2eat.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST("400", HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 요청입니다."),
    UNAUTHORIZED("401", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("403", HttpStatus.FORBIDDEN, "접근이 금지되었습니다."),
    NOT_FOUND("404", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}
