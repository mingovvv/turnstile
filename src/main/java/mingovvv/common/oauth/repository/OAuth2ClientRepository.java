package mingovvv.common.oauth.repository;

import mingovvv.common.oauth.domain.OAuth2Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OAuth2Client Repository
 */
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Long> {

    /**
     * Client ID로 조회
     *
     * @param clientId Client ID
     * @return OAuth2Client
     */
    Optional<OAuth2Client> findByClientId(String clientId);

    /**
     * Client ID로 활성화된 클라이언트 조회
     *
     * @param clientId Client ID
     * @param enabled  활성화 여부
     * @return OAuth2Client
     */
    Optional<OAuth2Client> findByClientIdAndEnabled(String clientId, Boolean enabled);

}
