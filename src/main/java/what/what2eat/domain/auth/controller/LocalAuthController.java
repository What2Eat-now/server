package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import what.what2eat.domain.auth.controller.dto.LocalAuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.LocalAuthResponseDTO;
import what.what2eat.domain.auth.service.LocalAuthService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "로컬 로그인 관련 컨트롤러", description = "로컬 로그인, 회원가입, 로그아웃 API")
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    @PostMapping("/signup/local")
    @Operation(summary = "로컬 회원가입", description = "로컬 회원가입을 처리합니다. 이메일, 비밀번호, 닉네임을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody LocalAuthRequestDTO.SignUpRequestDTO request) {
        localAuthService.signUp(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.CREATED));
    }

    @PostMapping("/login/local")
    @Operation(summary = "로컬 로그인", description = "로컬 로그인을 처리합니다. 이메일, 비밀번호를 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<LocalAuthResponseDTO.LoginResponseDTO>> login(@Valid @RequestBody LocalAuthRequestDTO.LoginRequestDTO request) throws Exception {
        LocalAuthResponseDTO.LoginResponseDTO login = localAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.of(login));
    }


}
