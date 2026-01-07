package mingovvv.common.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.oauth.dto.OAuth2TokenRequest;
import mingovvv.common.oauth.dto.OAuth2TokenResponse;
import mingovvv.common.oauth.service.OAuth2TokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 2.0 Token Endpoint
 * <p>
 * RFC 6749 표준 OAuth 2.0 Token 발급 API
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth 2.0", description = "OAuth 2.0 Token API (Client Credentials Grant)")
@ConditionalOnProperty(name = "feature.db.enabled", havingValue = "true")
public class OAuth2TokenController {

    private final OAuth2TokenService tokenService;

    /**
     * OAuth 2.0 Token Endpoint
     * <p>
     * Client Credentials Grant Type으로 Access Token을 발급합니다.
     * <p>
     * RFC 6749 표준:
     * - Content-Type: application/x-www-form-urlencoded
     * - grant_type=client_credentials
     *
     * @param request OAuth2TokenRequest
     * @return OAuth2TokenResponse
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(
            summary = "OAuth 2.0 Token 발급",
            description = "Client Credentials Grant Type으로 Access Token을 발급합니다. " +
                    "서버 간 통신(M2M)에 사용됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = OAuth2TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (grant_type, scope 오류)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (client_id 또는 client_secret 오류)"
            )
    })
    public OAuth2TokenResponse token(@Valid @ModelAttribute OAuth2TokenRequest request) {
        log.info("Token request from client: {}", request.getClientId());
        return tokenService.issueToken(request);
    }

}
