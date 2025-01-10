package what.what2eat.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.exception.AuthErrorCode;
import what.what2eat.domain.auth.exception.MemberException;
import what.what2eat.domain.auth.repository.AuthRepository;
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

    public void delete(HttpServletRequest request) {

        // 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 토큰에서 userEmail 추출
        String userEmail = jwtProvider.getUserEmail(token);

        // user 조회
        Optional<User> userOpt = authRepository.findByUserEmail(userEmail);

        if (userOpt.isEmpty()) {
            throw new MemberException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 회원 탈퇴 처리
        userOpt.get().delete();
    }
}
