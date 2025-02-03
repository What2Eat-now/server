package what.what2eat.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.controller.dto.AuthRequestDTO;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.AuthException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.domain.CustomUserDetails;
import what.what2eat.global.security.jwt.JwtProvider;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommonAuthService {

    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;

    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void logout(HttpServletRequest request) {
        String token = resolveToken(request);

        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(AuthErrorCode.ALREADY_LOGOUT_USER);
        }
        jwtProvider.addTokenToBlackList(token);
    }

    public void validateToken(HttpServletRequest request) {
        String token = resolveToken(request);

        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public void delete() {

        User user = authRepository.findByUserIdAndUserStatus(jwtProvider.extractUserId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (user == null) {
            throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 회원 탈퇴 처리
        user.delete();
    }

    public AuthResponseDTO.GetUserInfoDTO getUserInfo(HttpServletRequest request) {
        validateToken(request);

        String token = resolveToken(request);

        String userEmail = jwtProvider.getUserEmail(token);

        User findUser = authRepository.findByUserEmailAndUserStatus(userEmail, UserStatus.ACTIVE).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return AuthResponseDTO.GetUserInfoDTO.builder()
                .nickName(findUser.getNickName())
                .userEmail(findUser.getUserEmail())
                .build();

    }

    public void updateUserInfo(AuthRequestDTO.UpdateInfoDTO request) {

        User user = authRepository.findByUserIdAndUserStatus(jwtProvider.extractUserId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 이메일 유효성 검사 후 닉네임 변경
        if (user.getUserEmail().equals(request.getUserEmail())) {
            user.updateNickName(request.getNickName());
        }

    }

    public AuthResponseDTO.LocalLoginResponseDTO refreshToken(AuthRequestDTO.TokenRefreshDTO request) {
        // refresh token 검증
        jwtProvider.validateToken(request.getRefreshToken());

        // 사용자 이메일 조회
        String userEmail = jwtProvider.getUserEmail(request.getRefreshToken());

        // 이메일로 사용자 정보 DB 조회
        User user = authRepository.findByUserEmailAndUserStatus(userEmail, UserStatus.ACTIVE).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 유저 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getUserId(),
                user.getUserEmail(),
                null,
                user.getNickName(),
                user.getProvider(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        // 현재 refreshToken 블랙 리스트에 추가
        jwtProvider.addTokenToBlackList(request.getRefreshToken());

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getEmail());

        return AuthResponseDTO.LocalLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
