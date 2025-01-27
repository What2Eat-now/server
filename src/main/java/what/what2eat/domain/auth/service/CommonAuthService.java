package what.what2eat.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.controller.dto.CommonAuthRequestDTO;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.MemberException;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.exception.CommonErrorCode;
import what.what2eat.global.exception.CustomException;
import what.what2eat.global.security.jwt.JwtProvider;

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
            throw new MemberException(AuthErrorCode.ALREADY_LOGOUT_USER);
        }
        jwtProvider.addTokenToBlackList(token);
    }

    public void validateToken(HttpServletRequest request) {
        String token = resolveToken(request);

        boolean isValid = jwtProvider.validateToken(token);

        if (!isValid) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }
    }

    public void delete() {

        User user = authRepository.findByUserIdAndUserStatus(jwtProvider.extractUserId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new MemberException(AuthErrorCode.USER_NOT_FOUND));

        if (user == null) {
            throw new MemberException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 회원 탈퇴 처리
        user.delete();
    }

    public void updateUserInfo(CommonAuthRequestDTO.UpdateInfoDTO request) {

        User user = authRepository.findByUserIdAndUserStatus(jwtProvider.extractUserId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new MemberException(AuthErrorCode.USER_NOT_FOUND));

        // 이메일 유효성 검사 후 닉네임 변경
        if (user.getUserEmail().equals(request.getUserEmail())) {
            user.updateNickName(request.getNickName());
        }

    }

}
