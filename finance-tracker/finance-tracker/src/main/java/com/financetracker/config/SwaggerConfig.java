package com.financetracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration.
 *
 * This sets up:
 * 1. API title, version, description shown in Swagger UI
 * 2. JWT Bearer token support in Swagger UI
 *    (so you can paste your token and test protected endpoints directly)
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AI Finance Tracker API",
        version = "1.0",
        description = "AI-powered personal finance tracker with JWT auth, " +
                      "expense tracking, and OpenAI-generated monthly insights."
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // Configuration is done via annotations above
}
