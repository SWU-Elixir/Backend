package BE_Elixir.Elixir.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    String DEFAULT_URL = "";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .servers(List.of(new Server().url(DEFAULT_URL))); // 기본 API URL 설정
    }

    public Info apiInfo() {
        return new Info()
                .title("Swagger")
                .description("REST API")
                .version("1.0.0");
    }
}
