package com.cimhans.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.frontend-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CIMHANS Mental Health Management System API")
                        .version("v1.0.0")
                        .description("""
                            Production-grade REST API for the CIMHANS Mental Health Appointment
                            & Case Management System.
                            
                            **Authentication:** All protected endpoints require a Bearer JWT token.
                            Use POST /api/v1/auth/login to obtain tokens.
                            
                            **Roles:** ADMIN | PSYCHIATRIST | PSYCHOLOGIST | RECEPTIONIST | PATIENT
                            """)
                        .contact(new Contact()
                                .name("CIMHANS IT Department")
                                .email("admin@cimhans.com"))
                        .license(new License().name("Private").url("#")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url(serverUrl).description("Production")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/v1/auth/login")));
    }
}
