package what.what2eat.domain.auth.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
import what.what2eat.domain.auth.service.AuthService;
import what.what2eat.global.response.ApiResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    // 카카오 로그인 페이지로 리다이렉트
    @GetMapping("/login/kakao/url")
    public ResponseEntity<Void> getKakaoLoginPage() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authService.buildKakaoAuthUrl())
                .build();
    }

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    public ResponseEntity<ApiResponse<AuthResponseDTO.LoginInfoDTO>> getAccessToken(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.ok(authService.getKakaoUserInfo(code)));
    }

}
