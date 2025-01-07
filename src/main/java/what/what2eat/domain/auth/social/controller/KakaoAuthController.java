package what.what2eat.domain.auth.social.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.social.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.social.service.KakaoAuthService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.security.jwt.JwtProvider;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "로그인 관련 컨트롤러", description = "로컬 or 소셜 로그인 API")
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인")
    public ResponseEntity<ApiResponse<KakaoAuthResponseDTO.LoginInfoDTO>> login(@RequestParam String kakaoAccessToken) {
        return ResponseEntity.ok(ApiResponse.ok(kakaoAuthService.getKakaoUserInfo(kakaoAccessToken)));
    }

    @PostMapping("/logout/kakao")
    @Operation(summary = "카카오 소셜 로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        kakaoAuthService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
