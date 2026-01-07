package mingovvv.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security UserDetailsService 구현체
 * <p>
 * 사용자 인증 시 데이터베이스에서 사용자 정보를 조회합니다.
 * 실제 프로젝트에서는 UserRepository를 주입받아 사용자 정보를 조회해야 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: UserRepository 주입 필요
    // private final UserRepository userRepository;

    /**
     * 사용자 이름(username)으로 사용자 정보를 조회합니다.
     *
     * @param username 사용자 이름 (로그인 ID, 이메일 등)
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // TODO: 실제 데이터베이스에서 사용자 조회
        // UserEntity user = userRepository.findByUsername(username)
        //         .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // TODO: 사용자 권한 조회
        // List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
        //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
        //         .toList();

        // TODO: 실제 구현 시 아래 테스트 코드를 제거하고 위 주석을 해제하세요.
        // 임시: 테스트용 하드코딩 사용자 반환
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password("$2a$10$dummyPasswordHashForTesting")  // BCrypt 해시
                    .authorities(List.of(
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_USER")
                    ))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }

}
