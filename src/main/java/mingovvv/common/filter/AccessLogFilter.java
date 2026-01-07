package mingovvv.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.filter.wrapper.CustomRequestWrapper;
import mingovvv.common.utils.MDCUtil;
import mingovvv.common.utils.NetworkUtil;
import mingovvv.common.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * HTTP 요청/응답에 대한 접근 로그(Access Log)를 기록하는 필터입니다.
 * <p>
 * 요청의 유형(Multipart, Stream, General)에 따라 바디 로깅 전략을 다르게 가져갑니다.
 * MDC를 활용하여 요청 컨텍스트 정보를 관리합니다.
 * <p>
 * 실행 순서: HIGHEST_PRECEDENCE + 1 (ExceptionHandlerFilter 다음에 실행)
 * - ExceptionHandlerFilter → AccessLogFilter → 기타 필터들
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter implements Ordered {

    private static final int MAX_BODY_LOG_LENGTH = 2000;

    /**
     * 로깅을 제외할 URI prefix 목록
     */
    private static final List<String> PASS_URI_PREFIXES = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/.well-known",
            "/favicon.ico"
    );

    /**
     * URI 정규식 패턴
     */
    private static final Pattern DOWNLOAD_PATTERN = Pattern.compile(".*/(download(s)?|export(s)?|file(s)?|attachment(s)?|image(s)?)(/.*)?$");
    private static final Pattern EXTENSION_PATTERN = Pattern.compile(".*\\.(xlsx|xls|csv|pdf|zip|tar|7z|exe|jpg|png|mp4|mp3)$");
    private static final Pattern STREAM_PATTERN = Pattern.compile(".*/stream/?(\\?.*)?$");

    private final AtomicLong sequence = new AtomicLong(0L);

    /**
     * 필터 적용 여부를 결정합니다.
     *
     * @param request HttpServletRequest
     * @return 로깅 제외 대상이면 true, 아니면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PASS_URI_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    /**
     * 실제 필터 로직을 수행합니다.
     * 요청 유형에 따라 래핑 및 로깅 전략을 분기합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Debug 레벨일 때만 헤더 로깅 수행
        if (log.isDebugEnabled()) {
            logHeaders(request);
        }

        String contentType = request.getContentType();
        String requestUri = request.getRequestURI();

        var isMultipart = isMultipartRequest(contentType);
        var isDownload = isDownloadResponse(requestUri);
        var isStream = isStreamResponse(requestUri);

        long startTime = System.currentTimeMillis();

        try {
            if (isMultipart || isDownload) {
                // Multipart 또는 Download 요청 - Body 로깅 제외
                initializeMDC(request, startTime);
                logRequestBasic(request);
                filterChain.doFilter(request, response);
                logResponseBasic(startTime);
            } else if (isStream) {
                // Stream/SSE 요청 - 요청 Body만 로깅, 응답 Body는 제외
                var requestWrapper = new CustomRequestWrapper(request);
                initializeMDC(requestWrapper, startTime);
                logRequestWithBody(requestWrapper);
                filterChain.doFilter(requestWrapper, response);
                logResponseBasic(startTime);
            } else {
                // 일반 JSON 요청 - 요청/응답 Body 모두 로깅
                var requestWrapper = new CustomRequestWrapper(request);
                var responseWrapper = new ContentCachingResponseWrapper(response);

                initializeMDC(requestWrapper, startTime);
                logRequestWithBody(requestWrapper);

                filterChain.doFilter(requestWrapper, responseWrapper);

                logResponseWithBody(responseWrapper, startTime);
                responseWrapper.copyBodyToResponse();
            }
        } finally {
            MDCUtil.clear();
        }

    }


    /**
     * 요청 정보를 기반으로 MDC 컨텍스트를 초기화합니다.
     *
     * @param request   HttpServletRequest
     * @param startTime 요청 시작 시간 (ms)
     */
    private void initializeMDC(HttpServletRequest request, long startTime) {
        long reqSeqId = sequence.incrementAndGet();

        MDCUtil.setValue(MDCUtil.REQUEST_SEQ_ID, String.valueOf(reqSeqId));
        MDCUtil.setValue(MDCUtil.REQUEST_METHOD, request.getMethod());
        MDCUtil.setValue(MDCUtil.REQUEST_START_TIME, String.valueOf(startTime));
        MDCUtil.setValue(MDCUtil.REQUEST_URI, request.getRequestURI());
        MDCUtil.setValue(MDCUtil.REQUEST_QUERY_STRING, request.getQueryString());
        MDCUtil.setValue(MDCUtil.ACCESS_IP_ADDRESS, NetworkUtil.getClientIp(request));
    }


    /**
     * 기본 요청 정보를 로깅합니다. (Body 제외)
     *
     * @param request HttpServletRequest
     */
    private void logRequestBasic(HttpServletRequest request) {
        String uri = buildUriWithQuery(request.getRequestURI(), request.getQueryString());
        log.info("req uri={}, method={}, ip={}", uri, request.getMethod(), NetworkUtil.getClientIp(request));
    }

    /**
     * 요청 정보와 Body를 함께 로깅합니다.
     *
     * @param request HttpServletRequest (Wrapper)
     */
    private void logRequestWithBody(HttpServletRequest request) {
        String uri = buildUriWithQuery(request.getRequestURI(), request.getQueryString());
        String body = extractBody(request);

        log.info("req uri={}, method={}, body={}, ip={}", uri, request.getMethod(), body, NetworkUtil.getClientIp(request));
    }

    /**
     * 응답 정보와 Body를 함께 로깅합니다.
     *
     * @param response  ContentCachingResponseWrapper
     * @param startTime 요청 시작 시간
     */
    private void logResponseWithBody(ContentCachingResponseWrapper response, long startTime) {
        String uri = MDCUtil.getValue(MDCUtil.REQUEST_URI);
        long totalTime = System.currentTimeMillis() - startTime;

        if ("/whoami".equalsIgnoreCase(uri)) {
            return;
        }

        String contentType = response.getContentType();
        if (contentType != null && contentType.contains("octet-stream")) {
            log.info("res uri={}, status={}, totalTime={}ms, body=skip(octet-stream)", uri, response.getStatus(), totalTime);
            return;
        }

        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        body = sanitizeAndTruncateBody(body);

        log.info("res uri={}, status={}, body={}, totalTime={}ms", uri, response.getStatus(), body, totalTime);
    }

    /**
     * 기본 응답 정보를 로깅합니다. (Body 제외)
     *
     * @param startTime 요청 시작 시간
     */
    private void logResponseBasic(long startTime) {
        String uri = MDCUtil.getValue(MDCUtil.REQUEST_URI);
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("res uri={}, totalTime={}ms", uri, totalTime);
    }

    // ====================== 유틸 메서드 ======================

    /**
     * URI와 QueryString을 조합합니다.
     */
    private String buildUriWithQuery(String uri, String queryString) {
        return StringUtils.isNotBlank(queryString) ? uri + "?" + queryString : uri;
    }

    /**
     * Request Wrapper에서 Body를 추출하고 정제합니다.
     */
    private String extractBody(HttpServletRequest request) {
        String body = null;
        if (request instanceof CustomRequestWrapper custom) {
            body = new String(custom.getBody(), StandardCharsets.UTF_8);
        } else if (request instanceof ContentCachingRequestWrapper wrapper) {
            body = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        return sanitizeAndTruncateBody(body);
    }

    /**
     * Body 문자열을 정제(개행 제거)하고 최대 길이를 제한합니다.
     *
     * @param body 원본 Body 문자열
     * @return 정제된 문자열
     */
    private String sanitizeAndTruncateBody(String body) {
        if (body == null) {
            return "";
        }
        body = StringUtil.stripNewlinesAndTabs(body);
        if (body.length() > MAX_BODY_LOG_LENGTH) {
            return body.substring(0, MAX_BODY_LOG_LENGTH) + "... (truncated)";
        }
        return body;
    }

    /**
     * 헤더 정보를 디버그 레벨로 로깅합니다.
     */
    private void logHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("Header: {}={}", headerName, request.getHeader(headerName));
        }
    }

    /**
     * Multipart 요청 여부를 확인합니다.
     */
    private boolean isMultipartRequest(String contentType) {
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 다운로드 관련 응답인지 확인합니다.
     */
    private boolean isDownloadResponse(String requestUri) {
        return DOWNLOAD_PATTERN.matcher(requestUri).matches() || EXTENSION_PATTERN.matcher(requestUri).matches();
    }

    /**
     * 스트림 관련 응답인지 확인합니다.
     */
    private boolean isStreamResponse(String requestUri) {
        return STREAM_PATTERN.matcher(requestUri).matches();
    }

    /**
     * 필터의 실행 순서를 지정합니다.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

}
