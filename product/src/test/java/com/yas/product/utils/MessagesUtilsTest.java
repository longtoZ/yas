package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_KnownKeyFormatsArgs() {
        String message = MessagesUtils.getMessage("CATEGORY_NOT_FOUND", 10);
        assertEquals("Category 10 is not found", message);
    }

    @Test
    void getMessage_UnknownKeyReturnsKey() {
        String message = MessagesUtils.getMessage("UNKNOWN_CODE");
        assertEquals("UNKNOWN_CODE", message);
    }
}
