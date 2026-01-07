package mingovvv.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.properties.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 Provider 클래스
 * <p>
 * HMAC-SHA256 알고리즘을 사용하여 JWT를 생성하고 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * JWT 서명에 사용할 SecretKey를 생성합니다.
     *
     * @return HMAC-SHA 알고리즘용 SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token을 생성합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @return JWT Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtProperties.getAccessTokenValidity());
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @return JWT Refresh Token 문자열
     */
    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, jwtProperties.getRefreshTokenValidity());
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @param validityInMs   토큰 유효 시간 (밀리초)
     * @return JWT 토큰 문자열
     */
    private String generateToken(Authentication authentication, long validityInMs) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMs);

        // 권한 정보를 콤마로 구분된 문자열로 변환
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userDetails.getUsername())                      // 사용자 식별자 (username)
                .claim("authorities", authorities)                    // 권한 정보
                .issuer(jwtProperties.getIssuer())                       // 발급자
                .issuedAt(now)                                           // 발급 시간
                .expiration(expiryDate)                                  // 만료 시간
                .signWith(getSigningKey(), Jwts.SIG.HS256)               // 서명 알고리즘 및 키
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 이름(username)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 권한 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 권한 문자열 (콤마로 구분)
     */
    public String getAuthoritiesFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("authorities", String.class);
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Claims 객체
     * @throws JwtException 토큰 파싱 실패 시
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Client Credentials Grant용 Access Token을 생성합니다.
     * <p>
     * 사용자 정보 없이 Client ID와 Scope만으로 토큰을 발급합니다.
     * 서버 간 통신(M2M) 시 사용됩니다.
     *
     * @param clientId     Client ID (subject로 사용)
     * @param scope        허용된 Scope (공백으로 구분)
     * @param validityInMs 토큰 유효 시간 (밀리초)
     * @return JWT Access Token 문자열
     */
    public String generateClientCredentialsToken(String clientId, String scope, long validityInMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .subject(clientId)                                       // Client ID를 subject로
                .claim("scope", scope)                                // Scope 정보
                .claim("grant_type", "client_credentials")         // Grant Type 명시
                .issuer(jwtProperties.getIssuer())                       // 발급자
                .issuedAt(now)                                           // 발급 시간
                .expiration(expiryDate)                                  // 만료 시간
                .signWith(getSigningKey(), Jwts.SIG.HS256)               // 서명 알고리즘 및 키
                .compact();
    }

    /**
     * JWT 토큰에서 Scope를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Scope 문자열 (공백으로 구분)
     */
    public String getScopeFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("scope", String.class);
    }

    /**
     * HTTP 헤더에서 JWT 토큰을 추출합니다.
     *
     * @param bearerToken "Bearer {token}" 형식의 헤더 값
     * @return JWT 토큰 문자열, 형식이 맞지 않으면 null
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getPrefix() + " ")) {
            return bearerToken.substring(jwtProperties.getPrefix().length() + 1);
        }
        return null;
    }

}
