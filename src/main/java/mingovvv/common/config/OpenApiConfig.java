package mingovvv.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String BASE_RESPONSE_SCHEMA = "BaseResponse";

    @Bean
    public OpenAPI openApi() {
        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSchemas(BASE_RESPONSE_SCHEMA, baseResponseSchema());

        return new OpenAPI()
                .components(components)
                .info(new Info().title("common-jdk25 API").version("v1"));
    }

    /**
     * 모든 API에 공통 에러 스키마를 자동 등록합니다.
     */
    @Bean
    public OperationCustomizer commonErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            addResponseIfMissing(responses, "400", "Bad Request");
            addResponseIfMissing(responses, "401", "Unauthorized");
            addResponseIfMissing(responses, "403", "Forbidden");
            addResponseIfMissing(responses, "404", "Not Found");
            addResponseIfMissing(responses, "429", "Too Many Requests");
            addResponseIfMissing(responses, "500", "Internal Server Error");
            return operation;
        };
    }

    private void addResponseIfMissing(ApiResponses responses, String code, String description) {
        if (responses.containsKey(code)) {
            return;
        }
        ApiResponse apiResponse = new ApiResponse()
                .description(description)
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<>().$ref("#/components/schemas/" + BASE_RESPONSE_SCHEMA))));
        responses.addApiResponse(code, apiResponse);
    }

    private Schema<?> baseResponseSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("boolean"))
                .addProperty("code", new Schema<>().type("string"))
                .addProperty("message", new Schema<>().type("string"))
                .addProperty("data", new Schema<>().type("object").nullable(true));
    }

}
