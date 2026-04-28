package com.yas.sampledata.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.junit.jupiter.api.Test;

class SwaggerConfigTest {

    @Test
    void annotations_arePresent() {
        OpenAPIDefinition openApi = SwaggerConfig.class.getAnnotation(OpenAPIDefinition.class);
        SecurityScheme securityScheme = SwaggerConfig.class.getAnnotation(SecurityScheme.class);

        assertNotNull(openApi);
        assertNotNull(securityScheme);
    }
}
