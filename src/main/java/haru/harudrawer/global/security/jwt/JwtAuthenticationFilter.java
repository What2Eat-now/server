package haru.harudrawer.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.global.exception.BaseErrorCode;
import haru.harudrawer.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            //  HTTP 헤더에서 "Authorization" 추출
            String token = resolveToken(request);

            //  토큰이 존재하고, 유효한지 검사
            if (token != null && jwtProvider.validateToken(token)) {
                //  토큰에서 username 추출
                String userEmail = jwtProvider.getUserEmail(token);

                //  DB에서 유저 정보 가져오기 (UserDetailsService)
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                //  인증 객체(UsernamePasswordAuthenticationToken) 생성
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                //  SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            // 다음 필터로 진행
            filterChain.doFilter(request, response);
        } catch (AuthException e) {
            // 필터 내에서 AuthException 발생 시 여기서 커스텀 응답
            handleAuthException(response, e);
        } catch (Exception e) {
            // 기타 예외는 500 처리
            handleOtherException(response, e);
        }

    }

    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleAuthException(HttpServletResponse response, AuthException e) throws IOException {
        BaseErrorCode errorCode = e.getErrorCode();

        // 여기서 HttpStatus와 메시지를 가져옴
        HttpStatus status = errorCode.getHttpStatus();
        String message = errorCode.getMessage();

        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), message);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void handleOtherException(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        ErrorResponse errorResponse = ErrorResponse.of("500", e.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
