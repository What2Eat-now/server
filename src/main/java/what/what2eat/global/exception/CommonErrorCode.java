package what.what2eat.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseErrorCode{

    /**
     * 공통 에러 처리
     */
    BAD_REQUEST("400", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED("401", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("403", HttpStatus.FORBIDDEN, "접근이 금지되었습니다."),
    NOT_FOUND("404", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    CONFLICT("409", HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    INVALID_TOKEN("401",HttpStatus.UNAUTHORIZED, "유효하지 않는 토큰입니다."),

    // S3 ERROR
    FAIL_S3_UPLOAD("500", HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 업로드에 실패했습니다."),
    FAIL_S3_DELETE("500", HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 삭제에 실패했습니다.");

    /**
     * auth 예외 처리
     */
    private final String code;
    private final HttpStatus httpStatus;
    private final String message;


}
