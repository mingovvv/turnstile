package mingovvv.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Java 8+ java.time 패키지를 기반으로 한 날짜/시간 유틸리티.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    // "yyyy-MM-dd HH:mm:ss"
    public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER_DEFAULT = DateTimeFormatter.ofPattern(PATTERN_DEFAULT);

    // "yyyy-MM-dd"
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);

    // "yyyyMMdd"
    public static final String PATTERN_COMPACT = "yyyyMMdd";
    public static final DateTimeFormatter FORMATTER_COMPACT = DateTimeFormatter.ofPattern(PATTERN_COMPACT);

    // ISO 8601
    public static final DateTimeFormatter FORMATTER_ISO = DateTimeFormatter.ISO_DATE_TIME;

    // 기본 타임존 (서울)
    public static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");


    /**
     * 현재 시간 (LocalDateTime)
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(KST_ZONE);
    }

    /**
     * 현재 날짜 (LocalDate)
     */
    public static LocalDate nowDate() {
        return LocalDate.now(KST_ZONE);
    }

    /**
     * 현재 시간 문자열 (yyyy-MM-dd HH:mm:ss)
     */
    public static String nowString() {
        return now().format(FORMATTER_DEFAULT);
    }

    public static String toString(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(FORMATTER_DEFAULT);
    }

    public static String toString(LocalDateTime dateTime, String pattern) {
        return dateTime == null ? "" : dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toDateString(LocalDate date) {
        return date == null ? "" : date.format(FORMATTER_DATE);
    }

    public static String toIsoString(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(FORMATTER_ISO);
    }

    public static LocalDateTime parse(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, FORMATTER_DEFAULT);
    }

    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, FORMATTER_DATE);
    }

    public static LocalDateTime parseIso(String isoStr) {
        return LocalDateTime.parse(isoStr, FORMATTER_ISO);
    }

    /**
     * 오늘 기준 며칠 전/후 계산
     */
    public static LocalDateTime plusDays(long days) {
        return now().plusDays(days);
    }

    public static LocalDateTime minusDays(long days) {
        return now().minusDays(days);
    }

    /**
     * 특정 날짜의 시작 시간 (00:00:00) 구하기
     */
    public static LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 특정 날짜의 마지막 시간 (23:59:59.999999999) 구하기
     */
    public static LocalDateTime atEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 이번 달의 첫째 날
     */
    public static LocalDate firstDayOfMonth() {
        return nowDate().withDayOfMonth(1);
    }

    /**
     * 이번 달의 마지막 날
     */
    public static LocalDate lastDayOfMonth() {
        return nowDate().withDayOfMonth(nowDate().lengthOfMonth());
    }

    /**
     * 두 날짜 사이의 일수 차이
     */
    public static long diffDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 두 시간 사이의 분 차이
     */
    public static long diffMinutes(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

}
