package com.javaricci.ApiRestFulWebFlux.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

// NOTA: No Spring WebFlux substituímos WebMvcConfigurer por WebFluxConfigurer.
// A anotação @EnableWebMvc é substituída por @EnableWebFlux.
// Em aplicações Spring Boot com WebFlux, @EnableWebFlux pode conflitar com
// a autoconfiguração, por isso é comentado abaixo — use somente se necessário.

@Configuration
// @EnableWebFlux  // Descomente apenas se precisar substituir a autoconfiguração do WebFlux
public class WebConfigWebFlux implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .exposedHeaders("*");
    }
}