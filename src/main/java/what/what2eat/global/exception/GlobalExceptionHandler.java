package what.what2eat.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 존재하지 않는 요청에 대한 예외
    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiResponse<String>> handleNoPageFoundException(Exception e) {
        log.error("GlobalExceptionHandler catch NoHandlerFoundException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new CustomException(ErrorCode.NOT_FOUND)));
    }

    // 커스텀 예외
    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException e) {
        log.error("handleCustomException() in GlobalExceptionHandler throw CustomException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(new CustomException(ErrorCode.BAD_REQUEST)));
    }

    // 기본 예외
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("handleException() in GlobalExceptionHandler throw Exception : {}", e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)));
    }
}
