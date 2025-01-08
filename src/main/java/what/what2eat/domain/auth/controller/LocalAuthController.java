package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import what.what2eat.domain.auth.controller.dto.LocalAuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.LocalAuthResponseDTO;
import what.what2eat.domain.auth.service.LocalAuthService;
import what.what2eat.global.response.ApiResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "로컬 로그인 관련 컨트롤러", description = "로컬 로그인, 회원가입, 로그아웃 API")
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    @PostMapping("/signup/local")
    @Operation(summary = "로컬 회원가입")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody LocalAuthRequestDTO.SignUpRequestDTO request) {
        localAuthService.signUp(request);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/login/local")
    @Operation(summary = "로컬 로그인")
    public ResponseEntity<ApiResponse<LocalAuthResponseDTO.LoginResponseDTO>> login(@RequestBody LocalAuthRequestDTO.LoginRequestDTO request) throws Exception {
        LocalAuthResponseDTO.LoginResponseDTO login = localAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.ok(login));
    }

}
