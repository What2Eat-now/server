package haru.harudrawer.global.response;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    String code;
    String message;
    @Nullable T data;

    // 커스터마이징 가능한 of 메서드
    public static <T> ApiResponse<T> of(@Nullable T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> of(ResponseCode responseCode, @Nullable T data) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> of(ResponseCode responseCode) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
    }

}
