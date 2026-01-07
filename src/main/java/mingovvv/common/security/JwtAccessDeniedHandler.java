package mingovvv.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.constants.ResultCode;
import mingovvv.common.model.BaseResponse;
import mingovvv.common.model.BaseResponseFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증된 사용자가 권한이 없는 리소스에 접근 시 호출되는 Handler
 * <p>
 * HTTP 403 Forbidden 응답을 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 접근 거부 시 호출되는 메서드
     *
     * @param request               HttpServletRequest
     * @param response              HttpServletResponse
     * @param accessDeniedException 접근 거부 예외
     * @throws IOException 입출력 예외
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        log.error("Access denied error: {}", accessDeniedException.getMessage());

        // JSON 응답 생성 (ACCESS-001: Access denied.)
        BaseResponse<Void> errorResponse = BaseResponseFactory.create(ResultCode.Error.ACCESS_DENIED);

        // 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 응답 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
