package mingovvv.common.utils;

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
     * 휴대폰 번호 마스킹 (010-1234-5678 -> 010-****-5678)
     * (01012345678 -> 010****5678)
     */
    public static String phone(String phone) {
        if (!StringUtils.hasText(phone)) return phone;
        // 하이픈 있는 경우
        if (phone.contains("-")) {
            return phone.replaceAll("(\\d{2,3})-\\d{3,4}-(\\d{4})", "$1-****-$2");
        }
        // 하이픈 없는 경우 (11자리 기준)
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 이름 마스킹 (김밍고 -> 김*고, 이밍 -> 이*)
     */
    public static String name(String name) {
        if (!StringUtils.hasText(name)) return name;

        if (name.length() == 2) {
            return name.replaceAll("(?<=.{1}).", "*"); // 이밍 -> 이*
        }
        // 3글자 이상: 첫 글자와 마지막 글자 제외하고 마스킹
        return name.replaceAll("(?<=.{1}).(?=.{1})", "*"); // 김밍고 -> 김*고
    }

    /**
     * 카드 번호 등 기타 민감 정보 (뒷 4자리만 남기기)
     */
    public static String maskExceptLast4(String text) {
        if (!StringUtils.hasText(text) || text.length() < 4) return text;
        return "*".repeat(text.length() - 4) + text.substring(text.length() - 4);
    }

}
