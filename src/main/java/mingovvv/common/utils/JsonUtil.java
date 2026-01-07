package mingovvv.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JSON 직렬화/역직렬화를 위한 유틸리티 클래스
 * <p>
 * Spring Bean으로 관리되는 ObjectMapper를 사용하여 thread-safety와 설정 일관성을 보장합니다.
 * 기존 static 메서드 호출 방식을 유지하면서도 Bean 관리의 이점을 활용합니다.
 */
@Slf4j
@Component
public class JsonUtil {

    private static ObjectMapper objectMapper;

    /**
     * Spring Bean으로 관리되는 ObjectMapper를 주입받아 static 필드에 할당합니다.
     * 이를 통해 기존 static 메서드 호출 방식을 유지하면서도 Bean 관리의 이점을 활용할 수 있습니다.
     *
     * @param objectMapper JacksonConfig에서 생성된 ObjectMapper Bean
     */
    public JsonUtil(ObjectMapper objectMapper) {
        JsonUtil.objectMapper = objectMapper;
    }

    /**
     * Object -> JSON String
     */
    public static String toJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("ToJson Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Object -> Pretty JSON String
     */
    public static String toPrettyJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("ToPrettyJson Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> Object (단일 객체)
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("ToObject Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> Object (복잡한 제네릭: List, Map 등)
     * 사용법: JsonUtil.toObject(json, new TypeReference<List<MemberDto>>(){});
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("ToObject(TypeRef) Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> JsonNode (트리 구조 탐색용)
     */
    public static JsonNode readTree(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("ReadTree Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * ObjectNode 생성 (JSON 조립용)
     */
    public static ObjectNode createNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * List 변환 편의 메서드
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("ToList Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 원본 Mapper가 필요할 때
     */
    public static ObjectMapper getMapper() {
        return objectMapper;
    }

}
