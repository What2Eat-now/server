package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.service.CommonAuthService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth 관련 공통 API 컨트롤러", description = "로그아웃, 회원 탈퇴 등 공통 API를 처리합니다.")
public class CommonAuthController {

    private final CommonAuthService commonAuthService;

    @PostMapping("/logout")
    @Operation(summary = "공통 로그아웃", description = "로그아웃을 처리합니다. Authorization 헤더에 accessToken을 첨부해서 요청하시면 됩니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> logout(HttpServletRequest request) {
        commonAuthService.logout(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @DeleteMapping("")
    public ResponseEntity<ApiResponse<ResponseCode>> withdrawal(HttpServletRequest request) {
        commonAuthService.delete(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }
}
