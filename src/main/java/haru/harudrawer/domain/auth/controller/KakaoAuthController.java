package haru.harudrawer.domain.auth.controller;


import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.service.social.SocialAuthService;
import haru.harudrawer.global.response.ApiResponse;
import haru.harudrawer.global.response.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@Tag(name = "카카오 소셜 로그인 컨트롤러", description = " 카카오 소셜 로그인 API를 처리합니다. 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
public class KakaoAuthController {

    private final SocialAuthService socialAuthService;

    public KakaoAuthController(@Qualifier("kakaoAuthService") SocialAuthService socialAuthService) {
        this.socialAuthService = socialAuthService;
    }

    // 카카오 로그인 후 토큰과 사용자 정보 반환받음
    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인", description = "카카오 소셜 로그인을 처리합니다. kakaoAccessToken을 제공해야 합니다.")
    public ResponseEntity<ApiResponse<CommonResponseDTO.LoginResponseDTO>> login(@RequestParam String kakaoAccessToken) throws Exception {
        CommonResponseDTO.LoginResponseDTO loginResult = socialAuthService.login(kakaoAccessToken);

        return ResponseEntity.ok(ApiResponse.of(loginResult));
    }

    @DeleteMapping("/kakao")
    @Operation(summary = "카카오 회원 탈퇴", description = "카카오 소셜 회원 탈퇴(연결 해제)를 처리합니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> delete() throws Exception {
        socialAuthService.delete();

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

}
