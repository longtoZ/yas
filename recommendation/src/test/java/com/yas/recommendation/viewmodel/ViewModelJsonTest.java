package com.yas.recommendation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ViewModelJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void productDetailVm_serializesWithoutNulls() throws Exception {
        ProductDetailVm vm = new ProductDetailVm(
                1L,
                "Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        String json = objectMapper.writeValueAsString(vm);

        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Name\""));
        assertFalse(json.contains("shortDescription"));
    }

    @Test
    void relatedProductVm_deserializesJsonProperties() throws Exception {
        String json = "{" +
                "\"id\":5," +
                "\"name\":\"Phone\"," +
                "\"price\":12.5," +
                "\"brandName\":\"Brand\"," +
                "\"metaTitle\":\"Title\"," +
                "\"metaDescription\":\"Meta\"," +
                "\"specification\":\"Spec\"," +
                "\"slug\":\"slug\"," +
                "\"unknown\":\"ignore\"" +
                "}";

        RelatedProductVm vm = objectMapper.readValue(json, RelatedProductVm.class);

        assertEquals(5L, vm.getProductId());
        assertEquals("Phone", vm.getName());
        assertEquals(new BigDecimal("12.5"), vm.getPrice());
        assertEquals("Brand", vm.getBrand());
        assertEquals("Title", vm.getTitle());
        assertEquals("Meta", vm.getMetaDescription());
        assertEquals("Spec", vm.getSpecification());
        assertEquals("slug", vm.getSlug());
    }

    @Test
    void productVariationVm_ignoresUnknownFields() throws Exception {
        String json = "{" +
                "\"id\":7," +
                "\"name\":\"Variation\"," +
                "\"options\":{\"1\":\"Red\"}," +
                "\"unknown\":\"ignore\"" +
                "}";

        ProductVariationVm vm = objectMapper.readValue(json, ProductVariationVm.class);

        assertEquals(7L, vm.id());
        assertEquals("Variation", vm.name());
        assertNotNull(vm.options());
        assertEquals("Red", vm.options().get(1L));
    }

    @Test
    void categoryVm_ignoresUnknownFields() throws Exception {
        String json = "{" +
                "\"id\":2," +
                "\"name\":\"Category\"," +
                "\"unknown\":\"ignore\"" +
                "}";

        CategoryVm vm = objectMapper.readValue(json, CategoryVm.class);

        assertEquals(2L, vm.id());
        assertEquals("Category", vm.name());
    }

    @Test
    void imageVm_ignoresUnknownFields() throws Exception {
        String json = "{" +
                "\"id\":3," +
                "\"url\":\"/img.png\"," +
                "\"unknown\":\"ignore\"" +
                "}";

        ImageVm vm = objectMapper.readValue(json, ImageVm.class);

        assertEquals(3L, vm.id());
        assertEquals("/img.png", vm.url());
    }

    @Test
    void productAttributeValueVm_ignoresUnknownFields() throws Exception {
        String json = "{" +
                "\"id\":11," +
                "\"nameProductAttribute\":\"Color\"," +
                "\"value\":\"Black\"," +
                "\"unknown\":\"ignore\"" +
                "}";

        ProductAttributeValueVm vm = objectMapper.readValue(json, ProductAttributeValueVm.class);

        assertEquals(11L, vm.id());
        assertEquals("Color", vm.nameProductAttribute());
        assertEquals("Black", vm.value());
    }
}
