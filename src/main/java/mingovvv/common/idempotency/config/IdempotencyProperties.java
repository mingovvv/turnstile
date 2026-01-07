package mingovvv.common.idempotency.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@ConfigurationProperties(prefix = "feature.idempotency")
public class IdempotencyProperties {

    /**
     * 아이템포턴시 기능 on/off 스위치입니다.
     */
    private boolean enabled = false;

    /**
     * 아이템포턴시 키를 읽을 헤더 이름입니다.
     */
    private String headerName = "Idempotency-Key";

    /**
     * 캐시 TTL(초)입니다.
     */
    private long ttlSeconds = 300;

    /**
     * 아이템포턴시를 적용할 HTTP 메서드입니다.
     */
    private Set<String> methods = Set.of("POST", "PUT");

    /**
     * 아이템포턴시 사용 여부를 설정합니다.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 아이템포턴시 헤더명을 설정합니다.
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * 캐시 TTL(초)을 설정합니다.
     */
    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 아이템포턴시를 적용할 HTTP 메서드를 설정합니다.
     */
    public void setMethods(Set<String> methods) {
        this.methods = methods;
    }

}
