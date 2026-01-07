package mingovvv.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * String 관련 공통 유틸리티 메서드를 모아두는 클래스입니다.
 *
 * <p>
 * 인스턴스를 생성할 필요가 없으므로 {@code final} 클래스 + private 생성자로 정의합니다.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    /**
     * 문자열에서 개행(\n), 캐리지 리턴(\r), 탭(\t)을 모두 공백 하나로 치환하고,
     * 연속된 공백을 하나로 축소한 뒤, 양 끝 공백을 제거합니다.
     *
     * <pre>
     * 예)
     *  input : "\n  hello\tworld \r\n test"
     *  output: "hello world test"
     * </pre>
     * <p>
     * 주로 로그 출력용으로 사용하기 위한 유틸입니다.
     *
     * @param value 정제할 원본 문자열 (null 허용)
     * @return 개행/탭이 제거된 한 줄 문자열. 입력이 null 이면 null 반환.
     */
    public static String stripNewlinesAndTabs(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 개행/캐리지리턴/탭 → 공백 하나로 치환
        String normalized = value.replaceAll("[\\t\\n\\r]+", " ");

        // 연속된 공백을 하나로 축소
        normalized = normalized.replaceAll(" {2,}", " ");

        // 양 끝 공백 제거
        return normalized.trim();
    }

}