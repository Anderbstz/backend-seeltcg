package com.pikacards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI pikacardsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PikaCards API")
                        .description("API del e-commerce de cartas Pokémon TCG")
                        .version("1.0.0")
                        .contact(new Contact().name("PikaCards").email("contacto@pikacards.com")));
    }
}
