package what.what2eat.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.global.exception.CustomException;
import what.what2eat.global.exception.ErrorCode;
import what.what2eat.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutService {

    private final JwtProvider jwtProvider;

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
            throw new CustomException(ErrorCode.ALREADY_BLACKLIST);
        }
        jwtProvider.addTokenToBlackList(token);
    }
}
