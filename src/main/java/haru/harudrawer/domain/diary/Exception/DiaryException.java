package haru.harudrawer.domain.diary.Exception;

import haru.harudrawer.global.exception.BaseErrorCode;
import haru.harudrawer.global.exception.CustomException;

public class DiaryException extends CustomException {
    public DiaryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
