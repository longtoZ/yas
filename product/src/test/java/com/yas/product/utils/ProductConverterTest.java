package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProductConverterTest {

    @Test
    void toSlug_TrimsLowercasesAndReplaces() {
        String slug = ProductConverter.toSlug("  Hello, World!  ");
        assertEquals("hello-world-", slug);
    }

    @Test
    void toSlug_RemovesLeadingHyphen() {
        String slug = ProductConverter.toSlug("**Hello");
        assertEquals("hello", slug);
    }

    @Test
    void toSlug_CollapsesMultipleHyphens() {
        String slug = ProductConverter.toSlug("a---b");
        assertEquals("a-b", slug);
    }
}
