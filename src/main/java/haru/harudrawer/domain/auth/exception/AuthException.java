package haru.harudrawer.domain.auth.exception;

import lombok.Getter;
import haru.harudrawer.global.exception.BaseErrorCode;
import haru.harudrawer.global.exception.CustomException;

@Getter
public class AuthException extends CustomException {

    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
