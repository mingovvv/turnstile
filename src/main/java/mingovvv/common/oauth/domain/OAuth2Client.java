package mingovvv.common.oauth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OAuth 2.0 Client 엔티티
 * <p>
 * Client Credentials Grant Type을 위한 클라이언트 정보를 저장합니다.
 * 서버 간 통신(M2M) 시 사용됩니다.
 */
@Entity
@Table(name = "oauth2_clients",
        indexes = {
                @Index(name = "idx_client_id", columnList = "client_id", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Client ID (공개 식별자)
     * 예: "backend-service-a", "batch-scheduler"
     */
    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    /**
     * Client Secret (비밀 키, BCrypt 암호화)
     * 예: "$2a$10$..." (암호화된 값)
     */
    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    /**
     * 클라이언트 이름 (설명용)
     * 예: "백엔드 서비스 A", "배치 스케줄러"
     */
    @Column(name = "client_name", nullable = false, length = 200)
    private String clientName;

    /**
     * 허용된 Scope (공백으로 구분)
     * 예: "read:users write:users read:orders"
     */
    @Column(name = "scopes", nullable = false, length = 500)
    private String scopes;

    /**
     * 토큰 유효 시간 (초)
     * 기본값: 3600 (1시간)
     */
    @Column(name = "access_token_validity_seconds", nullable = false)
    private Integer accessTokenValiditySeconds = 3600;

    /**
     * 클라이언트 활성화 여부
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 설명 (용도, 담당자 등)
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Builder
    public OAuth2Client(String clientId, String clientSecret, String clientName,
                        String scopes, Integer accessTokenValiditySeconds,
                        Boolean enabled, String description) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.scopes = scopes;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds != null ? accessTokenValiditySeconds : 3600;
        this.enabled = enabled != null ? enabled : true;
        this.description = description;
    }

    /**
     * 클라이언트가 특정 Scope를 가지고 있는지 확인
     *
     * @param scope 확인할 scope
     * @return 포함 여부
     */
    public boolean hasScope(String scope) {
        if (this.scopes == null || this.scopes.isBlank()) {
            return false;
        }
        return this.scopes.contains(scope);
    }

    /**
     * 클라이언트 비활성화
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * 클라이언트 활성화
     */
    public void enable() {
        this.enabled = true;
    }

}
