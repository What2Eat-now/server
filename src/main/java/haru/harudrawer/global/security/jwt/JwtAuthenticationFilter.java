package haru.harudrawer.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import haru.harudrawer.global.security.service.CustomUserDetailsService;

import java.io.IOException;

/**
 * UsernamePasswordAuthenticationFilter보다 앞단에서 실행되도록 등록하여, JWT 토큰 검증을 수행
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. HTTP 헤더에서 "Authorization" 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 유효한지 검사
        if (token != null && jwtProvider.validateToken(token)) {
            // 3. 토큰에서 username 추출
            String userEmail = jwtProvider.getUserEmail(token);

            // 4. DB에서 유저 정보 가져오기 (UserDetailsService)
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 5. 인증 객체(UsernamePasswordAuthenticationToken) 생성
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                    null,
                            userDetails.getAuthorities()
                    );

            // 6. SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

        }
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
