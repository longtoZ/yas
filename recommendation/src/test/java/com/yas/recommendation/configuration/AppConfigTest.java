package com.yas.recommendation.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    void objectMapperBean_isCreated() {
        AppConfig config = new AppConfig();

        assertNotNull(config.objectMapper());
    }
}
