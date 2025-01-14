package what.what2eat.domain.diary.Exception;

import what.what2eat.global.exception.BaseErrorCode;
import what.what2eat.global.exception.CustomException;

public class DiaryException extends CustomException {
    public DiaryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
