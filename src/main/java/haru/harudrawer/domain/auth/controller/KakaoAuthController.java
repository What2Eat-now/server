package haru.harudrawer.domain.auth.controller;


import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.service.KakaoAuthService;
import haru.harudrawer.global.response.ApiResponse;
import haru.harudrawer.global.response.ResponseCode;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "카카오 소셜 로그인 컨트롤러", description = " 카카오 소셜 로그인 API를 처리합니다. 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인", description = "카카오 소셜 로그인을 처리합니다. kakaoAccessToken을 제공해야 합니다.")
    public ResponseEntity<ApiResponse<CommonResponseDTO.LoginResponseDTO>> login(@RequestParam String kakaoAccessToken) {
        CommonResponseDTO.LoginResponseDTO loginResult = kakaoAuthService.login(kakaoAccessToken);

        if (loginResult.isRequiresSignup()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .body(ApiResponse.of(ResponseCode.NEED_SIGNUP,loginResult));
        }

        return ResponseEntity.ok(ApiResponse.of(loginResult));
    }

    @PostMapping("/signup/kakao")
    @Operation(summary = "카카오 회원가입", description = "카카오 소셜 회원가입을 처리합니다. 이메일, 닉네임을 제공해야 합니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> signup(@RequestBody SocialRequestDTO.SocialSignupDTO request) {
        kakaoAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResponseCode.CREATED));
    }

    @DeleteMapping("/kakao")
    @Operation(summary = "카카오 회원 탈퇴", description = "카카오 소셜 회원 탈퇴(연결 해제)를 처리합니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> delete() {
        kakaoAuthService.delete();

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

}
