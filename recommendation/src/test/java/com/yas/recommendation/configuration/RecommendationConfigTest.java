package com.yas.recommendation.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RecommendationConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RecommendationConfig.class)
            .withPropertyValues("yas.services.product=http://example");

    @Test
    void apiUrl_isBoundFromProperties() {
        contextRunner.run(context -> {
            RecommendationConfig config = context.getBean(RecommendationConfig.class);
            assertEquals("http://example", config.getApiUrl());
        });
    }
}
