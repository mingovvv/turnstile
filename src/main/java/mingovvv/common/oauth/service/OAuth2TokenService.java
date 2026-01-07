package mingovvv.common.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.constants.ResultCode;
import mingovvv.common.oauth.domain.OAuth2Client;
import mingovvv.common.oauth.dto.OAuth2TokenRequest;
import mingovvv.common.oauth.dto.OAuth2TokenResponse;
import mingovvv.common.oauth.exception.OAuth2Exception;
import mingovvv.common.oauth.repository.OAuth2ClientRepository;
import mingovvv.common.security.JwtTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth 2.0 Token Service
 * <p>
 * Client Credentials Grant Type 토큰 발급을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "feature.db.enabled", havingValue = "true")
public class OAuth2TokenService {

    private final OAuth2ClientRepository clientRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Client Credentials Grant Type으로 Access Token을 발급합니다.
     *
     * @param request OAuth2TokenRequest
     * @return OAuth2TokenResponse
     */
    public OAuth2TokenResponse issueToken(OAuth2TokenRequest request) {
        // 1. Grant Type 검증
        if (!"client_credentials".equals(request.getGrantType())) {
            log.error("Invalid grant_type: {}", request.getGrantType());
            throw new OAuth2Exception(ResultCode.Error.OAUTH_UNSUPPORTED_GRANT_TYPE);
        }

        // 2. Client 조회
        OAuth2Client client = clientRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> {
                    log.error("Client not found: {}", request.getClientId());
                    return new OAuth2Exception(ResultCode.Error.OAUTH_INVALID_CLIENT);
                });

        // 3. Client 활성화 여부 확인
        if (!client.getEnabled()) {
            log.error("Client is disabled: {}", request.getClientId());
            throw new OAuth2Exception(ResultCode.Error.OAUTH_CLIENT_DISABLED);
        }

        // 4. Client Secret 검증
        if (!passwordEncoder.matches(request.getClientSecret(), client.getClientSecret())) {
            log.error("Invalid client_secret for client: {}", request.getClientId());
            throw new OAuth2Exception(ResultCode.Error.OAUTH_INVALID_CLIENT);
        }

        // 5. Scope 검증 및 처리
        String grantedScope = validateAndGrantScope(client, request.getScope());

        // 6. Access Token 생성
        long validityInMs = client.getAccessTokenValiditySeconds() * 1000L;
        String accessToken = jwtTokenProvider.generateClientCredentialsToken(
                client.getClientId(),
                grantedScope,
                validityInMs
        );

        // 7. 응답 생성
        log.info("Token issued for client: {}, scope: {}", client.getClientId(), grantedScope);

        return OAuth2TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValiditySeconds())
                .scope(grantedScope)
                .issuedAt(System.currentTimeMillis() / 1000)
                .build();
    }

    /**
     * 요청된 Scope를 검증하고 부여할 Scope를 결정합니다.
     *
     * @param client        OAuth2Client
     * @param requestedScope 요청된 Scope (null이면 모든 Scope 부여)
     * @return 부여된 Scope (공백으로 구분)
     */
    private String validateAndGrantScope(OAuth2Client client, String requestedScope) {
        // Client가 가진 모든 Scope
        Set<String> clientScopes = Arrays.stream(client.getScopes().split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // 요청한 Scope가 없으면 모든 Scope 부여
        if (requestedScope == null || requestedScope.isBlank()) {
            return String.join(" ", clientScopes);
        }

        // 요청한 Scope
        Set<String> requestedScopes = Arrays.stream(requestedScope.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // 요청한 Scope 중 Client가 가지지 않은 Scope가 있는지 확인
        Set<String> invalidScopes = new HashSet<>(requestedScopes);
        invalidScopes.removeAll(clientScopes);

        if (!invalidScopes.isEmpty()) {
            log.error("Invalid scope requested for client {}: {}", client.getClientId(), invalidScopes);
            throw new OAuth2Exception(ResultCode.Error.OAUTH_INVALID_SCOPE,
                    "Invalid scope: " + String.join(", ", invalidScopes));
        }

        // 교집합(요청한 Scope 중 Client가 가진 Scope만)
        requestedScopes.retainAll(clientScopes);

        return String.join(" ", requestedScopes);
    }

}
