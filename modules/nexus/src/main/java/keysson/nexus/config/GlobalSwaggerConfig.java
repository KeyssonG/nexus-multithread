package keysson.nexus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GlobalSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI nexusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus Multi-Thread Platform")
                        .version("1.0.0")
                        .description("Plataforma integrada de Gestão, Estoque e Administração."));
    }
}
