package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.request.LocalRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.LocalResponseDTO;
import what.what2eat.domain.auth.service.LocalAuthService;
import what.what2eat.global.response.ApiResponse;
import what.what2eat.global.response.ResponseCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "로컬 로그인 관련 컨트롤러", description = "로컬 로그인, 회원가입, 로그아웃 API")
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    @PostMapping("/signup/local")
    @Operation(summary = "로컬 회원가입", description = "로컬 회원가입을 처리합니다. 이메일, 비밀번호, 닉네임을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> signUp(@Valid @RequestBody LocalRequestDTO.SignUpRequestDTO request) throws MessagingException {
        localAuthService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResponseCode.CREATED));
    }

    @PostMapping("/login/local")
    @Operation(summary = "로컬 로그인", description = "로컬 로그인을 처리합니다. 이메일, 비밀번호를 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<LocalResponseDTO.LocalLoginResponseDTO>> login(@Valid @RequestBody LocalRequestDTO.LoginRequestDTO request) throws Exception {
        LocalResponseDTO.LocalLoginResponseDTO login = localAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.of(login));
    }

    @PostMapping("/check-email")
    @Operation(summary = "이메일 중복 체크", description = "이메일 중복 체크 및 소셜 로그인 유무 확인을 처리합니다. 이메일을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> checkEmail(@RequestParam String userEmail) {
        localAuthService.validateMember(userEmail);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PostMapping("/send-verification")
    @Operation(summary = "인증 번호 이메일 전송", description = "이메일로 인증 번호 전송을 처리합니다. 이메일을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> sendEmail(@RequestParam("userEmail") String userEmail) throws MessagingException {
        localAuthService.sendEmail(userEmail);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PostMapping("/verification-code")
    @Operation(summary = "인증번호 검증", description = "클라이언트로부터 전달받은 인증 번호 검증을 처리합니다. 이메일을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> verifyCode(@RequestBody LocalRequestDTO.VerifyCodeDTO request) {
        localAuthService.verifyCode(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));

    }

}
