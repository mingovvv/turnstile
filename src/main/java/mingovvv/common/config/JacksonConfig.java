package mingovvv.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Jackson ObjectMapper 설정을 관리하는 Configuration 클래스
 * <p>
 * ObjectMapper를 Spring Bean으로 관리하여 thread-safety와 설정 일관성을 보장합니다.
 * 애플리케이션 전체에서 동일한 ObjectMapper 인스턴스를 재사용합니다.
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper Bean을 생성합니다.
     * <p>
     * 설정:
     * - Java 8 Time API 지원 (JavaTimeModule)
     * - Optional, Stream 등 Java 8 기능 지원 (Jdk8Module)
     * - 생성자 파라미터 이름 인식 (ParameterNamesModule)
     * - 알 수 없는 프로퍼티 무시 (API 버전 호환성)
     * - 빈 객체 직렬화 허용
     * - 날짜를 타임스탬프 대신 ISO-8601 형식으로 직렬화
     * - 타임존을 Asia/Seoul로 고정
     *
     * @return 설정된 ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 모듈 등록
        mapper.registerModule(new ParameterNamesModule());  // 생성자 파라미터 이름 인식
        mapper.registerModule(new Jdk8Module());            // Optional, Stream 등 Java 8 지원
        mapper.registerModule(new JavaTimeModule());        // LocalDateTime, LocalDate 등 지원

        // Deserialization 설정
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // 알 수 없는 필드 무시

        // Serialization 설정
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);          // 빈 객체 직렬화 허용
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);    // 날짜를 ISO-8601 형식으로

        // 타임존 설정 (날짜 데이터 일관성 보장)
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return mapper;
    }

}
