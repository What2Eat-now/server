package what.what2eat.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.global.security.jwt.JwtAuthenticationFilter;
import what.what2eat.global.security.jwt.JwtProvider;
import what.what2eat.global.security.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 비활성 / 폼 로그인 비활성 등
        http.csrf(csrf -> csrf.disable());
        http.formLogin(formLogin -> formLogin.disable());

        // 2. 인증이 필요없는 URL 설정
        //    (로그인, 회원가입, 토큰 재발급 등은 permitAll)
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/restaurants/**").hasAuthority(Role.USER.name())
                .anyRequest().authenticated());

        // 3. 커스텀 필터 추가
        //    UsernamePasswordAuthenticationFilter 앞에 JWT 필터를 두어, 토큰 검증이 먼저 수행되도록
        http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userDetailsService)
                , UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 패스워드 관련 여러 인코딩 알고리즘 사용을 제공하는 DelegatingPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
