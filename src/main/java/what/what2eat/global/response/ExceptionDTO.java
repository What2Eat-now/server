package what.what2eat.global.response;

import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * ApiResponse에서 error를 담당할 클래스
 */

@Getter
public class ExceptionDTO {

    // Custom Code
    @NotNull
    private final String code;

    @NotNull
    private final String message;

    public ExceptionDTO(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public static ExceptionDTO of(ErrorCode errorCode) {
        return new ExceptionDTO(errorCode);
    }
}
