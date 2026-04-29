package com.yas.payment.paypal.service;

import com.paypal.core.PayPalHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayPalHttpClientInitializerTest {

    private final PayPalHttpClientInitializer initializer = new PayPalHttpClientInitializer();

    @Test
    void createPaypalClient_whenAdditionalSettingsIsNull_throwIllegalArgumentException() {
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> initializer.createPaypalClient(null));
        assertNotNull(exception);
    }

    @Test
    void createPaypalClient_whenModeIsSandbox_returnPaypalHttpClient() {
        String additionalSettings = "{\"clientId\":\"client-id\",\"clientSecret\":\"client-secret\",\"mode\":\"sandbox\"}";

        PayPalHttpClient paypalHttpClient = initializer.createPaypalClient(additionalSettings);

        assertNotNull(paypalHttpClient);
    }

    @Test
    void createPaypalClient_whenModeIsLive_returnPaypalHttpClient() {
        String additionalSettings = "{\"clientId\":\"client-id\",\"clientSecret\":\"client-secret\",\"mode\":\"live\"}";

        PayPalHttpClient paypalHttpClient = initializer.createPaypalClient(additionalSettings);

        assertNotNull(paypalHttpClient);
    }

    @Test
    void createPaypalClient_whenAdditionalSettingsMalformed_throwIllegalStateException() {
        String malformedAdditionalSettings = "not-a-json";

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> initializer.createPaypalClient(malformedAdditionalSettings));
        assertNotNull(exception);
    }
}
