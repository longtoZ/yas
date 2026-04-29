package com.yas.product.model.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.product.model.Product;
import org.junit.jupiter.api.Test;

class AttributeModelEqualsHashCodeTest {

    @Test
    void productAttribute_equalsWithSameId_isTrue() {
        ProductAttribute first = new ProductAttribute();
        first.setId(1L);
        ProductAttribute second = new ProductAttribute();
        second.setId(1L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productAttribute_equalsWithSameInstance_isTrue() {
        ProductAttribute attribute = new ProductAttribute();

        assertTrue(attribute.equals(attribute));
    }

    @Test
    void productAttribute_equalsWithDifferentType_isFalse() {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(1L);

        assertFalse(attribute.equals("not-attribute"));
    }

    @Test
    void productAttribute_equalsWithNullId_isFalse() {
        ProductAttribute first = new ProductAttribute();
        ProductAttribute second = new ProductAttribute();

        assertFalse(first.equals(second));
    }

    @Test
    void productAttributeGroup_equalsWithSameId_isTrue() {
        ProductAttributeGroup first = new ProductAttributeGroup();
        first.setId(2L);
        ProductAttributeGroup second = new ProductAttributeGroup();
        second.setId(2L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productAttributeGroup_equalsWithSameInstance_isTrue() {
        ProductAttributeGroup group = new ProductAttributeGroup();

        assertTrue(group.equals(group));
    }

    @Test
    void productAttributeGroup_equalsWithNullId_isFalse() {
        ProductAttributeGroup first = new ProductAttributeGroup();
        ProductAttributeGroup second = new ProductAttributeGroup();

        assertFalse(first.equals(second));
    }

    @Test
    void productTemplate_equalsWithSameId_isTrue() {
        ProductTemplate first = new ProductTemplate();
        first.setId(3L);
        ProductTemplate second = new ProductTemplate();
        second.setId(3L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productTemplate_equalsWithSameInstance_isTrue() {
        ProductTemplate template = new ProductTemplate();

        assertTrue(template.equals(template));
    }

    @Test
    void productTemplate_equalsWithNullId_isFalse() {
        ProductTemplate first = new ProductTemplate();
        ProductTemplate second = new ProductTemplate();

        assertFalse(first.equals(second));
    }

    @Test
    void productTemplate_builderDefaults_notNullCollections() {
        ProductTemplate template = ProductTemplate.builder().build();

        assertNotNull(template.getProductAttributeTemplates());
    }

    @Test
    void productAttributeTemplate_builderStoresValues() {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(10L);
        ProductTemplate template = new ProductTemplate();
        template.setId(11L);

        ProductAttributeTemplate attributeTemplate = ProductAttributeTemplate.builder()
                .id(12L)
                .productAttribute(attribute)
                .productTemplate(template)
                .displayOrder(4)
                .build();

        assertEquals(12L, attributeTemplate.getId());
        assertEquals(10L, attributeTemplate.getProductAttribute().getId());
        assertEquals(11L, attributeTemplate.getProductTemplate().getId());
        assertEquals(4, attributeTemplate.getDisplayOrder());
    }

    @Test
    void productAttributeValue_settersPersistValues() {
        ProductAttributeValue value = new ProductAttributeValue();
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(20L);
        Product product = new Product();
        product.setId(21L);

        value.setId(22L);
        value.setProduct(product);
        value.setProductAttribute(attribute);
        value.setValue("Cotton");

        assertEquals(22L, value.getId());
        assertEquals(21L, value.getProduct().getId());
        assertEquals(20L, value.getProductAttribute().getId());
        assertEquals("Cotton", value.getValue());
    }
}
