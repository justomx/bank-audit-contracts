package com.justo.bank.template.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation.
 *
 * <p>With the app running:
 * <ul>
 *   <li>UI: {@code http://localhost:8080/api/swagger-ui.html}</li>
 *   <li>Spec: {@code http://localhost:8080/api/v3/api-docs}</li>
 * </ul>
 *
 * <p>In production deployments, Swagger UI must remain disabled
 * ({@code springdoc.swagger-ui.enabled=false}). See {@code application.yml}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.application.name}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API")
                        .description("Bank vertical service — Justo")
                        .version("v1")
                        .contact(new Contact().name("Justo Engineering").url("https://justo.mx"))
                        .license(new License().name("Internal")));
    }
}
