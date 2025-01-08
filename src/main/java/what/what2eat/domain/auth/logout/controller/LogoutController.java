package what.what2eat.domain.auth.logout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import what.what2eat.domain.auth.logout.service.LogoutService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "로그아웃 컨트롤러", description = "로그아웃 API")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping("/logout")
    @Operation(summary = "공통 로그아웃")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        logoutService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(ResponseCode.SUCCESS.getMessage()));
    }
}
