package what.what2eat.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.request.AppleRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.AppleResponseDTO;
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
    public ResponseEntity<ApiResponse<AppleResponseDTO.AppleLoginResponseDTO>> login(@RequestParam String authorizationCode) throws Exception {
        AppleResponseDTO.AppleLoginResponseDTO result = appleAuthService.login(authorizationCode);

        if (result.isRequireSignup()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .body(ApiResponse.of(ResponseCode.NEED_SIGNUP, result));
        }

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS, result));
    }

    @PostMapping("/signup/apple")
    public ResponseEntity<ApiResponse<ResponseCode>> signup(@RequestBody AppleRequestDTO.AppleSignupDTO request) {
        appleAuthService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResponseCode.CREATED));
    }
}
