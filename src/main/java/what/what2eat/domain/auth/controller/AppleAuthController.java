package what.what2eat.domain.auth.controller;

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
    public ResponseEntity<ApiResponse<CommonResponseDTO.LoginResponseDTO>> login(@RequestParam String authorizationCode) throws Exception {
        CommonResponseDTO.LoginResponseDTO result = appleAuthService.login(authorizationCode);

        if (result.isRequiresSignup()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .body(ApiResponse.of(ResponseCode.NEED_UPDATE_NICKNAME, result));
        }

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS, result));
    }
}
