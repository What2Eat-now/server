package haru.harudrawer.global.response;

import lombok.Getter;

/**
 * 공통 에러 형식
 */

@Getter
public class ErrorResponse {

    // Custom Code
    private final String errorCode;

    private final String errorMessage;

    public ErrorResponse(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ErrorResponse of(String errorCode, String errorMessage) {
        return new ErrorResponse(errorCode, errorMessage);
    }

}
