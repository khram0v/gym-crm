package io.github.khram0v.gymcrm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(title = "Gym CRM API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer logoutPathCustomizer() {
        return openApi -> openApi.getPaths().addPathItem("api/v1/auth/logout",
                new PathItem().post(new Operation()
                        .tags(List.of("Auth"))
                        .summary("Logout and invalidate the current JWT access token")
                        .security(List.of(new io.swagger.v3.oas.models.security.SecurityRequirement()
                                .addList("bearerAuth")))
                        .responses(new ApiResponses()
                                .addApiResponse("204", new ApiResponse().description("Logged out"))))
        );
    }
}
