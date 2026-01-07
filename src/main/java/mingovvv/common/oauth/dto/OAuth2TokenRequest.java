package mingovvv.common.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth 2.0 Token 요청 DTO
 * <p>
 * Client Credentials Grant Type을 위한 요청 파라미터
 */
@Getter
@NoArgsConstructor
public class OAuth2TokenRequest {

    /**
     * Grant Type (반드시 "client_credentials")
     */
    @NotBlank(message = "grant_type is required")
    private String grantType;

    /**
     * Client ID
     */
    @NotBlank(message = "client_id is required")
    private String clientId;

    /**
     * Client Secret
     */
    @NotBlank(message = "client_secret is required")
    private String clientSecret;

    /**
     * 요청할 Scope (선택 사항, 공백으로 구분)
     * 예: "read:users write:orders"
     */
    private String scope;

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
