package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.request.AppleRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.AppleResponseDTO;
import what.what2eat.domain.auth.controller.dto.response.CommonResponseDTO;
import what.what2eat.domain.auth.controller.dto.response.LocalResponseDTO;
import what.what2eat.domain.auth.service.AppleAuthService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AppleAuthController {
    private final AppleAuthService appleAuthService;

    @PostMapping("/login/apple")
    @Operation(summary = "애플 로그인", description = "애플 소셜 로그인을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<CommonResponseDTO.LoginResponseDTO>> login(@RequestParam String authorizationCode) throws Exception {
        CommonResponseDTO.LoginResponseDTO result = appleAuthService.login(authorizationCode);

        if (result.isRequiresSignup()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .body(ApiResponse.of(ResponseCode.NEED_UPDATE_NICKNAME, result));
        }

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS, result));
    }

    @DeleteMapping("/apple")
    @Operation(summary = "애플 회원 탈퇴", description = "애플 회원 탈퇴(토큰 회수)를 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> delete(@RequestParam String authorizationCode) throws Exception {
        appleAuthService.revokeAppleToken(authorizationCode);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }
}
