package com.yas.sampledata.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RestClientConfigTest {

    @Test
    void restClient_isCreated() {
        RestClientConfig config = new RestClientConfig();

        assertNotNull(config.restClient());
    }
}
