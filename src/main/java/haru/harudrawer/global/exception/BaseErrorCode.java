package haru.harudrawer.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
