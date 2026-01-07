package mingovvv.common.config;

import lombok.RequiredArgsConstructor;
import mingovvv.common.security.JwtAccessDeniedHandler;
import mingovvv.common.security.JwtAuthenticationEntryPoint;
import mingovvv.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정 클래스
 * <p>
 * JWT 기반 Stateless 인증 방식을 사용하며, CORS 정책을 설정합니다.
 * 메서드 레벨 보안 어노테이션(@PreAuthorize, @Secured 등)을 활성화합니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * Spring Security 필터 체인을 설정합니다.
     * <p>
     * 주요 설정:
     * - CSRF 비활성화 (JWT 사용으로 불필요)
     * - CORS 활성화 (corsConfigurationSource 사용)
     * - 세션 사용 안 함 (STATELESS)
     * - 경로별 인증/인가 설정
     * - Form 로그인, HTTP Basic 비활성화
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (JWT 기반 Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 경로별 인증/인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 공개 경로 (인증 불필요)
                        .requestMatchers(
                                "/api/auth/**",           // 인증 API (로그인, 회원가입 등)
                                "/api/public/**",         // 공개 API
                                "/oauth/token",           // OAuth 2.0 Token Endpoint
                                "/actuator/health",       // Health Check
                                "/actuator/info",         // Info
                                "/swagger-ui/**",         // Swagger UI
                                "/v3/api-docs/**",        // OpenAPI Docs
                                "/mock/**",               // Mock API (Swagger 확인용)
                                "/h2-console/**",         // H2 Console (로컬 개발용)
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // Actuator 엔드포인트는 인증 필요
                        .requestMatchers("/actuator/**").authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // H2 Console 사용을 위한 설정 (로컬 개발용)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                // 예외 처리 설정 (인증 실패, 접근 거부)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401 처리
                        .accessDeniedHandler(jwtAccessDeniedHandler)            // 403 처리
                );

        // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 앞에 배치)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정을 정의합니다.
     * <p>
     * 설정:
     * - 허용 Origin: 모든 Origin 허용 (운영 환경에서는 특정 도메인으로 제한 필수)
     * - 허용 메서드: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * - 허용 헤더: 모든 헤더 허용
     * - 인증 정보 포함 허용 (Authorization 헤더 등)
     * - Preflight 요청 캐싱: 3600초 (1시간)
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정 (운영 환경에서는 특정 도메인으로 제한)
        // configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://yourdomain.com"));
        configuration.setAllowedOriginPatterns(List.of("*"));  // 모든 Origin 허용

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // 브라우저가 접근할 수 있는 응답 헤더
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // Preflight 요청 결과를 캐싱하는 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용

        return source;
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean을 생성합니다.
     * <p>
     * BCrypt 알고리즘을 사용하며, strength 기본값은 10입니다.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
