package mingovvv.common.mockapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mingovvv.common.model.BaseResponse;
import mingovvv.common.model.BaseResponseFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Mock API", description = "Swagger 확인용 샘플 API")
@RestController
@RequestMapping("/mock")
public class MockApiController {

    @Operation(summary = "Mock ping")
    @GetMapping("/ping")
    public BaseResponse<Map<String, Object>> ping() {
        return BaseResponseFactory.create(Map.of("answer", "pong"));
    }

}
