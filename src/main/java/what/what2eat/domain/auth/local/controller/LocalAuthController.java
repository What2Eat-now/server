package what.what2eat.domain.auth.local.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import what.what2eat.domain.auth.local.controller.dto.LocalAuthRequestDTO;
import what.what2eat.domain.auth.local.controller.dto.LocalAuthResponseDTO;
import what.what2eat.domain.auth.local.service.LocalAuthService;
import what.what2eat.global.response.ApiResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LocalAuthController {


    private final LocalAuthService localAuthService;

    @PostMapping("/signup/local")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody LocalAuthRequestDTO.SignUpRequestDTO request) {
        localAuthService.signUp(request);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/login/local")
    public ResponseEntity<ApiResponse<LocalAuthResponseDTO.LoginResponseDTO>> login(@RequestBody LocalAuthRequestDTO.LoginRequestDTO request) throws Exception {
        LocalAuthResponseDTO.LoginResponseDTO login = localAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.ok(login));
    }
}
