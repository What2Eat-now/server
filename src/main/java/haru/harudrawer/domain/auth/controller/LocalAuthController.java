package haru.harudrawer.domain.auth.controller;

import haru.harudrawer.domain.auth.controller.dto.request.LocalRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.service.LocalAuthService;
import haru.harudrawer.global.response.ApiResponse;
import haru.harudrawer.global.response.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<ApiResponse<CommonResponseDTO.LoginResponseDTO>> login(@Valid @RequestBody LocalRequestDTO.LoginRequestDTO request) throws Exception {
        CommonResponseDTO.LoginResponseDTO login = localAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.of(login));
    }

    @DeleteMapping("/local")
    @Operation(summary = "로컬 회원탈퇴", description = "회원탈퇴를 처리합니다. Authorization 헤더에 accessToken을 첨부해서 요청하시면 됩니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> withdrawal() {
        localAuthService.delete();

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PostMapping("/check-email/signup")
    @Operation(summary = "이메일 중복 체크(회원가입)", description = "이메일 중복 체크 및 소셜 로그인 유무 확인을 처리합니다. 이메일을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> checkEmailForSignup(@RequestParam String userEmail) {
        localAuthService.validateEmailForSignup(userEmail);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PostMapping("/check-email/recovery")
    @Operation(summary = "이메일 중복 체크(비밀번호 찾기)", description = "이메일 중복 체크 및 소셜 로그인 유무 확인을 처리합니다. 이메일을 제공해야 합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> checkEmailForRecovery(@RequestParam String userEmail) {
        localAuthService.validateEmailForRecovery(userEmail);

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

    @PostMapping("/local/find-email")
    @Operation(summary = "아이디(이메일) 찾기", description = "전화번호를 통해 잃어버린 이메일을 조회합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> findEmail(@RequestBody LocalRequestDTO.FindEmailDTO request) {
        String userEmail = localAuthService.findUserEmail(request);

        return ResponseEntity.ok(ApiResponse.of(Map.of("userEmail", userEmail)));
    }

    @PostMapping("/local/reset-password")
    @Operation(summary = "비밀번호 찾기 및 변경", description = "이메일을 통해 잃어버린 비밀번호를 변경합니다. \n 응답코드에 따른 결과값은 포스트맨 API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> resetPassword(@RequestBody LocalRequestDTO.ResetPasswordDTO request) {
        localAuthService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

}
