package com.javaricci.ApiRestFulWebFlux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiRestFulWebFluxSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiRestFulWebFluxSpringBootApplication.class, args);
    }

    //WEB
    //http://localhost:8080/fornecedores

    //API
    //http://localhost:8080/api/fornecedores
    //http://localhost:8080/api/fornecedores/8

    //SWAGGER-UI
    //http://localhost:8080/swagger-ui.html

    //Relatórios Jaspersoft Studio community edition 7.0.3
    //http://localhost:8080/api/fornecedores/relatorio
}