package what.what2eat.global.response;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    public static <T> ApiResponse<T> fail(CustomException e) {
        return new ApiResponse<>(e.getErrorCode().getCode(), false, null, ExceptionDTO.of(e.getErrorCode()));
    }

}
