package what.what2eat.domain.auth.exception;

import lombok.Getter;
import what.what2eat.global.exception.BaseErrorCode;
import what.what2eat.global.exception.CustomException;

@Getter
public class AuthException extends CustomException {

    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
