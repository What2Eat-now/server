package what.what2eat.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 공통 에러 처리
     */
    BAD_REQUEST("400", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED("401", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("403", HttpStatus.FORBIDDEN, "접근이 금지되었습니다."),
    NOT_FOUND("404", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    CONFLICT("409", HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),

    /**
     * auth 예외 처리
     */

    USER_NOT_FOUND("404", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("409", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    INVALID_USER_ROLE("403", HttpStatus.FORBIDDEN, "유효하지 않은 사용자 권한입니다."),
    ALREADY_BLACKLIST("409", HttpStatus.CONFLICT, "이미 블랙리스트에 포함된 토큰입니다."),
    INVALID_PASSWORD("422", HttpStatus.UNPROCESSABLE_ENTITY, "비밀번호 형식이 잘못되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}
