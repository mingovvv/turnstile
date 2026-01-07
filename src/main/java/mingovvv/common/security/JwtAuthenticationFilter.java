package mingovvv.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.properties.JwtProperties;
import mingovvv.common.constants.ResultCode;
import mingovvv.common.model.BaseResponse;
import mingovvv.common.model.BaseResponseFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터 (엄격한 방식)
 * <p>
 * HTTP 요청 헤더에서 JWT 토큰을 추출하고 검증하여 Spring Security Context에 인증 정보를 설정합니다.
 * <p>
 * 인증 정책:
 * 1. 토큰이 없는 경우 → 다음 필터로 진행 (SecurityConfig가 판단)
 * 2. 토큰이 있고 유효한 경우 → SecurityContext에 인증 정보 설정 후 진행
 * 3. 토큰이 있지만 유효하지 않은 경우 → 즉시 401 응답 반환 (필터 체인 중단)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    /**
     * JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정합니다.
     * <p>
     * 엄격한 정책:
     * - 토큰이 있으면 반드시 유효해야 합니다.
     * - 유효하지 않은 토큰은 즉시 401 응답을 반환합니다.
     *
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @param filterChain FilterChain
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 1. 헤더에서 JWT 토큰 추출
        String token = extractTokenFromRequest(request);

        // 2. 토큰이 없는 경우 → 그냥 다음 필터로 (공개 API 허용)
        if (token == null) {
            log.debug("No JWT token found in request headers for uri: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰이 있는 경우 → 반드시 유효해야 함
        if (!jwtTokenProvider.validateToken(token)) {
            // 유효하지 않은 토큰 → 즉시 401 응답
            log.error("Invalid JWT token for request uri: {}", requestUri);
            sendUnauthorizedResponse(response, ResultCode.Error.AUTH_TOKEN_INVALID);
            return;
        }

        // 4. 토큰이 유효한 경우 → 인증 정보 설정
        try {
            setAuthenticationToSecurityContext(token, request);
            log.debug("Successfully authenticated request for uri: {}", requestUri);
        } catch (Exception e) {
            // 인증 정보 설정 중 예외 발생 → 즉시 401 응답
            log.error("Failed to set authentication for uri: {}, error: {}", requestUri, e.getMessage(), e);
            sendUnauthorizedResponse(response, ResultCode.Error.AUTH_TOKEN_INVALID);
            return;  // 여기서 필터 체인 중단!
        }

        // 5. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출하여 SecurityContext에 인증 정보를 설정합니다.
     *
     * @param token   JWT 토큰
     * @param request HttpServletRequest
     */
    private void setAuthenticationToSecurityContext(String token, HttpServletRequest request) {
        // 1. 토큰에서 사용자 정보 추출
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // 2. 권한 정보 추출 (authorities 또는 scope 시도)
        String authoritiesString = jwtTokenProvider.getAuthoritiesFromToken(token);
        if (authoritiesString == null || authoritiesString.isBlank()) {
            // Client Credentials의 경우 scope를 권한으로 사용
            String scope = jwtTokenProvider.getScopeFromToken(token);
            authoritiesString = scope;
        }

        // 3. 권한 정보 파싱
        List<SimpleGrantedAuthority> authorities = parseAuthorities(authoritiesString);

        // 4. UserDetails 객체 생성
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")  // 비밀번호는 JWT에 포함되지 않음
                .authorities(authorities)
                .build();

        // 5. Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 6. SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Set Authentication to security context for '{}', uri: {}", username, request.getRequestURI());
    }

    /**
     * 인증 실패 시 401 Unauthorized 응답을 전송합니다.
     *
     * @param response   HttpServletResponse
     * @param resultCode 에러 코드
     * @throws IOException 입출력 예외
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, ResultCode.Error resultCode) throws IOException {

        BaseResponse<Void> errorResponse = BaseResponseFactory.create(resultCode);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출합니다.
     *
     * @param request HttpServletRequest
     * @return JWT 토큰, 없으면 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeader());
        return jwtTokenProvider.resolveToken(bearerToken);
    }

    /**
     * 권한 문자열을 파싱하여 SimpleGrantedAuthority 리스트로 변환합니다.
     *
     * @param authoritiesString 콤마 또는 공백으로 구분된 권한 문자열
     *                          예: "ROLE_USER,ROLE_ADMIN" 또는 "read write admin"
     * @return SimpleGrantedAuthority 리스트
     */
    private List<SimpleGrantedAuthority> parseAuthorities(String authoritiesString) {
        if (authoritiesString == null || authoritiesString.isBlank()) {
            return List.of();
        }

        // 콤마 또는 공백으로 구분 (OAuth scope는 공백, 일반 권한은 콤마)
        String delimiter = authoritiesString.contains(",") ? "," : " ";

        return Arrays.stream(authoritiesString.split(delimiter))
                .map(String::trim)
                .filter(auth -> !auth.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
