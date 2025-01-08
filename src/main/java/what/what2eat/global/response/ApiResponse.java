package what.what2eat.global.response;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import what.what2eat.global.exception.CustomException;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    String code;
    boolean success;
    @Nullable T data;
    @Nullable ExceptionDTO error;

    public static <T> ApiResponse<T> ok(@Nullable T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), true ,data, null);
    }

    // 커스터마이징 가능한 of 메서드
    public static <T> ApiResponse<T> of(HttpStatus status, @Nullable T data) {
        return new ApiResponse<>(String.valueOf(status.value()), true, data, null);
    }

    // 데이터 포함하지 않은 실패 응답
    public static <T> ApiResponse<T> fail(CustomException e) {
        return new ApiResponse<>(e.getErrorCode().getCode(), false, null , ExceptionDTO.of(e.getErrorCode()));
    }


}
