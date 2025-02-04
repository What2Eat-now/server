package what.what2eat.global.config;

import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.global.security.jwt.JwtAuthenticationFilter;
import what.what2eat.global.security.jwt.JwtProvider;
import what.what2eat.global.security.service.CustomUserDetailsService;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import static what.what2eat.domain.auth.entity.Role.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    /**
     * permitAll 권한을 가진 엔드포인트에 적용되는 Security FilterChain
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainPermitAll(HttpSecurity http) throws Exception {
        configureCommonSecuritySettings(http);

        http.securityMatchers(matchers -> matchers.requestMatchers(requestPermitAll()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainAuthorized(HttpSecurity http) throws Exception {

        // http 객체 셋팅
        configureCommonSecuritySettings(http);

        // 인증이 필요한 URL 설정
        http.securityMatchers(matchers -> matchers.requestMatchers(requestHasRoleUser()))
                .authorizeHttpRequests(auth -> auth
                .anyRequest()
                .hasAuthority(USER.name()));

        // 커스텀 필터 추가
        //    UsernamePasswordAuthenticationFilter 앞에 JWT 필터를 두어, 토큰 검증이 먼저 수행되도록
        http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userDetailsService)
                , UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 인증 및 인가가 필요한 엔드포인트에 적용되는 RequestMatcher
    private RequestMatcher[] requestHasRoleUser() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher("/api/v1/groups/**"),
                antMatcher("/api/v1/diary/**"),
                antMatcher(HttpMethod.PUT ,"/api/v1/auth"),
                antMatcher(HttpMethod.DELETE, "/api/v1/auth")

        );

        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    // permitAll 권한을 가진 엔드포인트에 적용되는 RequestMatcher
    private RequestMatcher[] requestPermitAll() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher("/"),
                antMatcher("/swagger-ui/**"),
                antMatcher("/v3/api-docs/**"),
                antMatcher("/api/v1/auth/login/**"),
                antMatcher("/api/v1/auth/signup/**"),
                antMatcher("/api/v1/auth/reissue")
        );

        return requestMatchers.toArray(RequestMatcher[]::new);
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

    // Security 기본 셋팅
    private void configureCommonSecuritySettings(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // csrf disable
                .anonymous(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // form login disable
                .httpBasic(AbstractHttpConfigurer::disable)  // http basic 인증 방식 disable
                .rememberMe(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }
}
