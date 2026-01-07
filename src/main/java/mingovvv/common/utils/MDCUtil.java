package mingovvv.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MDCUtil {

    public static final String REQUEST_SEQ_ID = "seq";
    public static final String REQUEST_METHOD = "method";
    public static final String REQUEST_START_TIME = "startTime";
    public static final String REQUEST_URI = "uri";
    public static final String REQUEST_QUERY_STRING = "query";
    public static final String ACCESS_IP_ADDRESS = "clientIp";

    /**
     * MDC 값 설정 (Null-Safe)
     */
    public static void setValue(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * MDC 값 조회
     */
    public static String getValue(String key) {
        return MDC.get(key);
    }

    /**
     * MDC 전체 초기화
     */
    public static void clear() {
        MDC.clear();
    }

}