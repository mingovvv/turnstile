package mingovvv.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkUtil {

    public static final String UNKNOWN = "unknown";

    /**
     * Proxy, L4, Web Server 등을 거쳐온 실제 클라이언트 IP 구하기
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return UNKNOWN;

        String ip = request.getHeader("X-Forwarded-For");

        if (!isValidIp(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (!isValidIp(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (!isValidIp(ip)) ip = request.getHeader("HTTP_CLIENT_IP");
        if (!isValidIp(ip)) ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (!isValidIp(ip)) ip = request.getRemoteAddr();

        // "123.123.123.123, 10.0.0.1" 처럼 콤마로 여러 개 올 경우 첫 번째가 실제 IP
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }

        return ip;
    }

    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

}
