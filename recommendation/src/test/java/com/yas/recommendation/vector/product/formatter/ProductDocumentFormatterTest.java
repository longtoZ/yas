package com.yas.recommendation.vector.product.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductDocumentFormatterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void format_expandsAttributesAndCategories() {
        ProductDocumentFormatter formatter = new ProductDocumentFormatter();
        Map<String, Object> entity = new java.util.HashMap<>();
        entity.put("name", "Phone");
        entity.put("attributeValues", List.of(
                Map.of("id", 1L, "nameProductAttribute", "Color", "value", "Black"),
                Map.of("id", 2L, "nameProductAttribute", "Storage", "value", "128GB")));
        entity.put("categories", List.of(
                Map.of("id", 10L, "name", "Electronics"),
                Map.of("id", 11L, "name", "Phones")));
        entity.put("metaDescription", "<p>Great</p> phone");
        String template = "{name}|{attributeValues}|{categories}|{metaDescription}";

        String result = formatter.format(entity, template, objectMapper);

        assertEquals("Phone|[Color: Black, Storage: 128GB]|[Electronics, Phones]|Great phone", result);
    }

    @Test
    void format_handlesNullCollections() {
        ProductDocumentFormatter formatter = new ProductDocumentFormatter();
        Map<String, Object> entity = new java.util.HashMap<>();
        entity.put("name", "Phone");
        entity.put("attributeValues", null);
        entity.put("categories", null);
        String template = "{name}|{attributeValues}|{categories}";

        String result = formatter.format(entity, template, objectMapper);

        assertEquals("Phone|[]|[]", result);
    }
}
