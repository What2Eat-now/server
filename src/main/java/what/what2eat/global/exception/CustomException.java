package what.what2eat.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object detailData;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailData = null;
    }

    public CustomException(ErrorCode errorCode, Object detailData) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailData = detailData;
    }


}
