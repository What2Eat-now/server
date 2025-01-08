package what.what2eat.domain.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import what.what2eat.domain.auth.controller.dto.KakaoAuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.service.KakaoAuthService;
import what.what2eat.global.response.ApiResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "카카오 소셜 로그인 컨트롤러", description = " 카카오 소셜 로그인 API를 처리합니다. 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인", description = "카카오 소셜 로그인을 처리합니다. kakaoAccessToken을 제공해야 합니다.")
    public ResponseEntity<ApiResponse<KakaoAuthResponseDTO.LoginInfoDTO>> login(@RequestParam String kakaoAccessToken) {
        return ResponseEntity.ok(ApiResponse.ok(kakaoAuthService.login(kakaoAccessToken)));
    }

    @PostMapping("/signup/kakao")
    @Operation(summary = "카카오 회원가입", description = "카카오 소셜 회원가입을 처리합니다. 이메일, 닉네임을 제공해야 합니다.")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody KakaoAuthRequestDTO.KakaoSignupDTO request) {
        kakaoAuthService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null));
    }
}
