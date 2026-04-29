package com.yas.tax.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    private final ResourceBundle originalBundle = MessagesUtils.messageBundle;

    @AfterEach
    void tearDown() {
        MessagesUtils.messageBundle = originalBundle;
    }

    @Test
    void getMessage_WhenMessageCodeExists_ReturnFormattedMessage() {
        MessagesUtils.messageBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"tax.message", "Tax {} for {}"}
                };
            }
        };

        String result = MessagesUtils.getMessage("tax.message", "rate", "product");

        assertThat(result).isEqualTo("Tax rate for product");
    }

    @Test
    void getMessage_WhenMessageCodeMissing_ReturnErrorCode() {
        MessagesUtils.messageBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[0][];
            }
        };

        String result = MessagesUtils.getMessage("missing.code");

        assertThat(result).isEqualTo("missing.code");
    }
}
