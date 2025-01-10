package what.what2eat.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
