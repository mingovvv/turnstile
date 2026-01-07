package mingovvv.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 정보를 바인딩하는 Properties 클래스
 * <p>
 * application.yml의 jwt.* 속성들을 매핑합니다.
 */
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명에 사용되는 비밀키
     * 최소 256비트(32글자) 이상 권장
     */
    private final String secretKey;

    /**
     * Access Token 유효 시간 (밀리초)
     * 기본값: 3600000 (1시간)
     */
    private final long accessTokenValidity;

    /**
     * Refresh Token 유효 시간 (밀리초)
     * 기본값: 604800000 (7일)
     */
    private final long refreshTokenValidity;

    /**
     * JWT 발급자 (issuer)
     */
    private final String issuer;

    /**
     * JWT를 전달할 HTTP 헤더명
     * 기본값: Authorization
     */
    private final String header;

    /**
     * JWT 토큰 접두사
     * 기본값: Bearer
     */
    private final String prefix;

}
