package what.what2eat.domain.auth.social.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.social.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.social.service.KakaoAuthService;
import what.what2eat.global.response.ApiResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "로그인 관련 컨트롤러", description = "로컬 or 소셜 로그인 API")
public class KakaoAuthController {

    private final KakaoAuthService authService;

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인")
    public ResponseEntity<ApiResponse<KakaoAuthResponseDTO.LoginInfoDTO>> getAccessToken(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.ok(authService.getKakaoUserInfo(code)));
    }

}
