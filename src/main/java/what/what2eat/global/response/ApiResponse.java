package what.what2eat.global.response;

import jakarta.annotation.Nullable;
import what.what2eat.global.exception.CustomException;

public record ApiResponse<T>(
        String code,
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDTO error)
        {

        public static <T> ApiResponse<T> ok(@Nullable T data) {
            return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), true ,data, null);
        }

        public static <T> ApiResponse<T> created(@Nullable T data) {
            return new ApiResponse<>(ResponseCode.CREATED.getCode(), true, data, null);
        }

        public static <T> ApiResponse<T> fail(CustomException e) {
            return new ApiResponse<>(e.getErrorCode().getCode(), false, null, ExceptionDTO.of(e.getErrorCode()));
        }

}

// ResponseCode.SUCCESS.getCode()