package mingovvv.common.filter.wrapper;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomRequestWrapper extends HttpServletRequestWrapper {

    @Getter
    private final byte[] body;

    public CustomRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        String contentType = request.getContentType();

        // 1. Form Data (application/x-www-form-urlencoded) 처리
        // 톰캣이 이미 파싱해버린 파라미터를 다시 Body 문자열로 복원해야 함
        if (contentType != null && MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            this.body = restoreFormBody(request);
        }
        // 2. JSON, Text, XML 등 일반 Body 처리
        else {
            this.body = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    /**
     * 파라미터 맵을 다시 x-www-form-urlencoded 형식의 바이트 배열로 복원합니다.
     * 중요: 반드시 URL Encoding을 해야 특수문자가 깨지지 않습니다.
     */
    private byte[] restoreFormBody(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        String formString = parameterMap.entrySet().stream()
                .flatMap(entry -> {
                    String encodedKey = encode(entry.getKey());
                    return Arrays.stream(entry.getValue())
                            .map(value -> encodedKey + "=" + encode(value));
                })
                .collect(Collectors.joining("&"));

        return formString.getBytes(StandardCharsets.UTF_8);
    }

    private String encode(String value) {
        try {
            // 공백을 '+'가 아닌 '%20'으로 처리하려면 StandardCharsets.UTF_8 사용
            return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(this.body);
    }

    /**
     * Http Header를 Map으로 변환 (로깅용)
     */
    public Map<String, String> getHeaderMap() {
        return Collections.list(getHeaderNames())
                .stream()
                .collect(Collectors.toMap(name -> name, this::getHeader));
    }

    // ==============================================================
    //  Inner Class: CachedServletInputStream
    //  (ByteArrayInputStream을 ServletInputStream으로 감싸줌)
    // ==============================================================
    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream buffer;

        public CachedServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

}
