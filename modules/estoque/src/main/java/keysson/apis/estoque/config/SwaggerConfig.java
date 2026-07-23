package keysson.apis.estoque.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("estoqueSwaggerConfig")
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi estoqueApi() {
        return GroupedOpenApi.builder()
                .group("estoque")
                .pathsToMatch("/estoque/**")
                .build();
    }
}
