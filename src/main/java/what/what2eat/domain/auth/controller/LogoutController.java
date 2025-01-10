package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import what.what2eat.domain.auth.service.LogoutService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "로그아웃 컨트롤러", description = "로그아웃 API")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping("/logout")
    @Operation(summary = "공통 로그아웃", description = "로그아웃을 처리합니다. Authorization 헤더에 accessToken을 첨부해서 요청하시면 됩니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        logoutService.logout(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS.getMessage()));
    }
}
