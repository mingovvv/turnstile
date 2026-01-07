package mingovvv.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskingUtil {

    /**
     * 이메일 마스킹 (mingo@gmail.com -> mi***@gmail.com)
     */
    public static String email(String email) {
        if (!StringUtils.hasText(email)) return email;
        return email.replaceAll("(?<=.{2}).(?=.*@)", "*");
    }

    /**
     * 전화번호 마스킹 (010-1234-5678 -> 010-****-5678)
     * (01012345678 -> 010****5678)
     */
    public static String phone(String phone) {
        if (!StringUtils.hasText(phone)) return phone;
        // 하이픈 포함
        if (phone.contains("-")) {
            return phone.replaceAll("(\\d{2,3})-\\d{3,4}-(\\d{4})", "$1-****-$2");
        }
        // 하이픈 미포함 (11자리 기준)
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 이름 마스킹 (홍길동 -> 홍*동, 홍길 -> 홍*)
     */
    public static String name(String name) {
        if (!StringUtils.hasText(name)) return name;

        if (name.length() == 2) {
            return name.replaceAll("(?<=.{1}).", "*"); // 홍길 -> 홍*
        }
        // 3자 이상: 가운데 글자 마스킹
        return name.replaceAll("(?<=.{1}).(?=.{1})", "*"); // 홍길동 -> 홍*동
    }

    /**
     * 마지막 4자리만 노출하고 나머지 마스킹
     */
    public static String maskExceptLast4(String text) {
        if (!StringUtils.hasText(text) || text.length() < 4) return text;
        return "*".repeat(text.length() - 4) + text.substring(text.length() - 4);
    }

    /**
     * Redis URL 인증 정보를 마스킹합니다. (user:password@host -> user:***@host)
     */
    public static String redisUrl(String url) {
        if (!StringUtils.hasText(url)) return url;
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            String maskedUserInfo = maskUserInfo(userInfo);
            URI safeUri = new URI(
                    uri.getScheme(),
                    maskedUserInfo,
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );
            return safeUri.toString();
        } catch (URISyntaxException ex) {
            return maskUrlBasic(url);
        }
    }

    private static String maskUserInfo(String userInfo) {
        if (!StringUtils.hasText(userInfo)) return null;
        int idx = userInfo.indexOf(':');
        if (idx < 0) return "***";
        return userInfo.substring(0, idx) + ":***";
    }

    private static String maskUrlBasic(String url) {
        int at = url.indexOf('@');
        if (at < 0) return url;
        int scheme = url.indexOf("://");
        if (scheme < 0) return "***@" + url.substring(at + 1);
        return url.substring(0, scheme + 3) + "***@" + url.substring(at + 1);
    }

}
