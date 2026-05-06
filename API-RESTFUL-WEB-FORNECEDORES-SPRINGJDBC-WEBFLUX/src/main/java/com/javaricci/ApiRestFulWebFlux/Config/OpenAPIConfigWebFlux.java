package com.javaricci.ApiRestFulWebFlux.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfigWebFlux {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API RESTFUL WEBFLUX - SQLITE")
                        .description("CADASTRO DE FORNECEDORES - Spring WebFlux")
                        .version("1.0.0"));
    }
}