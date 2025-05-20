package BE_Elixir.Elixir.global.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
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
public class SwaggerConfig {

    String DEFAULT_URL = "https://port-0-elixir-backend-g0424l70py8py.gksl2.cloudtype.app/";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .servers(List.of(new Server().url(DEFAULT_URL))); // 기본 API URL 설정
    }

    public Info apiInfo() {
        return new Info()
                .title("Elixir API")
                .description("API documentation for Elixir")
                .version("1.0.0");
    }
}
