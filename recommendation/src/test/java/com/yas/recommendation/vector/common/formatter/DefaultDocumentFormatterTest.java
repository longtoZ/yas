package com.yas.recommendation.vector.common.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultDocumentFormatterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void format_replacesPlaceholders_andStripsHtml() {
        DocumentFormatter formatter = new DefaultDocumentFormatter();
        Map<String, Object> entity = Map.of(
                "name", "Laptop",
                "description", "<b>Fast</b> laptop");
        String template = "{name}|{description}";

        String result = formatter.format(entity, template, objectMapper);

        assertEquals("Laptop|Fast laptop", result);
    }

    @Test
    void removeHtmlTags_handlesNullAndEmpty() {
        DocumentFormatter formatter = new DefaultDocumentFormatter();

        assertNull(formatter.removeHtmlTags(null));
        assertEquals("", formatter.removeHtmlTags(""));
    }
}
