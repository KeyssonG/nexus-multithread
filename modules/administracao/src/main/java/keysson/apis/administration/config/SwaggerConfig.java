package keysson.apis.administration.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("administrationSwaggerConfig")
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi administrationApi() {
        return GroupedOpenApi.builder()
                .group("administration")
                .pathsToMatch("/administracao/**")
                .build();
    }
}
