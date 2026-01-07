package mingovvv.common.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 2.0 Token 응답 DTO
 * <p>
 * RFC 6749 표준 형식
 */
@Getter
@Builder
public class OAuth2TokenResponse {

    /**
     * Access Token (JWT)
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token Type (항상 "Bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 토큰 유효 시간 (초)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * 부여된 Scope (공백으로 구분)
     */
    @JsonProperty("scope")
    private String scope;

    /**
     * 발급 시간 (Unix Timestamp)
     * OAuth 2.0 표준은 아니지만 유용함
     */
    @JsonProperty("issued_at")
    private Long issuedAt;

}
