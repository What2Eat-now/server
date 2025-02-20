package what.what2eat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.auth.controller.dto.request.CommonRequestDTO;
import what.what2eat.domain.auth.controller.dto.response.CommonResponseDTO;
import what.what2eat.domain.auth.controller.dto.response.LocalResponseDTO;
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

    @PutMapping("/me/email")
    @Operation(summary = "회원 이메일 수정", description = "회원 이메일 수정을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<LocalResponseDTO.LocalLoginResponseDTO>> updateUserEmail(@RequestBody CommonRequestDTO.UpdateEmailDTO request) {

        return ResponseEntity.ok(ApiResponse.of(commonAuthService.updateUserEmail(request)));
    }


    @PutMapping("/me/nickname")
    @Operation(summary = "회원 닉네임 수정", description = "회원 닉네임 수정을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> updateUserNickName(@RequestBody CommonRequestDTO.UpdateNickNameDTO request) {

        commonAuthService.updateUserNickName(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @PutMapping("/me/password")
    @Operation(summary = "회원 비밀번호 수정", description = "회원 비밀번호 수정을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<ResponseCode>> updateUserPassword(@RequestBody CommonRequestDTO.UpdatePasswordDTO request) {

        commonAuthService.updateUserPassword(request);

        return ResponseEntity.ok(ApiResponse.of(ResponseCode.SUCCESS));
    }

    @GetMapping("/me")
    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회를 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<CommonResponseDTO.GetUserInfoDTO>> getUserInfo(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.of(commonAuthService.getUserInfo(request)));
    }

    @PostMapping("/reissue")
    @Operation(summary = "Access Token 재발급", description = "Access Token 재발급을 처리합니다. \n 응답 코드에 따른 자세한 결과는 PostMan API 명세서를 참고 부탁드립니다.")
    public ResponseEntity<ApiResponse<LocalResponseDTO.LocalLoginResponseDTO>> refreshAccessToken(@RequestBody CommonRequestDTO.TokenRefreshDTO request) {
        LocalResponseDTO.LocalLoginResponseDTO token = commonAuthService.refreshToken(request);


        return ResponseEntity.ok(ApiResponse.of(token));

    }

}
