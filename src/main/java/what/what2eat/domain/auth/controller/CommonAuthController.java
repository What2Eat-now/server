package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.AuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
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
    @Operation(summary = "공통 회원탈퇴", description = "회원탈퇴를 처리합니다. Authorization 헤더에 accessToken을 첨부해서 요청하시면 됩니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> withdrawal() {
        commonAuthService.delete();

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PostMapping("/validate-token")
    @Operation(summary = "토큰 유효성 검증", description = "Access Token에 대해 유효성 검사를 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")

    public ResponseEntity<ApiResponse<ResponseCode>> validateToken(HttpServletRequest request) {
        commonAuthService.validateToken(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.CONFIRM));
    }

    @PutMapping("/me")
    @Operation(summary = "회원 정보 수정", description = "회원 정보 수정을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> updateUserInfo(@RequestBody AuthRequestDTO.UpdateInfoDTO request) {
        commonAuthService.updateUserInfo(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<AuthResponseDTO>> getUserInfo(HttpServletRequest request) {
//
//
//    }

    @PostMapping("/reissue")
    @Operation(summary = "Access Token 재발급", description = "Access Token 재발급을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<AuthResponseDTO.LocalLoginResponseDTO>> refreshAccessToken(@RequestBody AuthRequestDTO.TokenRefreshDTO request) {
        AuthResponseDTO.LocalLoginResponseDTO token = commonAuthService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.of(token));

    }

}
