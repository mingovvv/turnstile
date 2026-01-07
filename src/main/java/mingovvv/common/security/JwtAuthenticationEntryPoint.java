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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근 시 호출되는 EntryPoint
 * <p>
 * HTTP 401 Unauthorized 응답을 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 호출되는 메서드
     *
     * @param request       HttpServletRequest
     * @param response      HttpServletResponse
     * @param authException 인증 예외
     * @throws IOException 입출력 예외
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        log.error("Unauthorized error: {}", authException.getMessage());

        // JSON 응답 생성 (AUTH-001: Authentication is required.)
        BaseResponse<Void> errorResponse = BaseResponseFactory.create(ResultCode.Error.AUTH_REQUIRED);

        // 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 응답 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
