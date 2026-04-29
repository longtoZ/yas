package com.yas.sampledata.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_returnsKeyWhenMissing() {
        String message = MessagesUtils.getMessage("UNKNOWN_CODE");

        assertEquals("UNKNOWN_CODE", message);
    }
}
