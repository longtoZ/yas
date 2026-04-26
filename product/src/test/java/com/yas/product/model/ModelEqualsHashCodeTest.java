package com.yas.product.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ModelEqualsHashCodeTest {

    @Test
    void product_equalsWithSameId_isTrue() {
        Product first = new Product();
        first.setId(1L);
        Product second = new Product();
        second.setId(1L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void product_equalsWithSameInstance_isTrue() {
        Product product = new Product();

        assertTrue(product.equals(product));
    }

    @Test
    void product_equalsWithNullId_isFalse() {
        Product first = new Product();
        Product second = new Product();

        assertFalse(first.equals(second));
    }

    @Test
    void product_equalsWithDifferentType_isFalse() {
        Product product = new Product();
        product.setId(1L);

        assertFalse(product.equals("not-a-product"));
    }

    @Test
    void product_builderDefaults_notNullCollections() {
        Product product = Product.builder().build();

        assertNotNull(product.getRelatedProducts());
        assertNotNull(product.getProductCategories());
        assertNotNull(product.getAttributeValues());
        assertNotNull(product.getProductImages());
        assertNotNull(product.getProducts());
    }

    @Test
    void brand_equalsWithSameId_isTrue() {
        Brand first = new Brand();
        first.setId(2L);
        Brand second = new Brand();
        second.setId(2L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void brand_equalsWithSameInstance_isTrue() {
        Brand brand = new Brand();

        assertTrue(brand.equals(brand));
    }

    @Test
    void brand_equalsWithNullId_isFalse() {
        Brand first = new Brand();
        Brand second = new Brand();

        assertFalse(first.equals(second));
    }

    @Test
    void category_equalsWithSameId_isTrue() {
        Category first = new Category();
        first.setId(3L);
        Category second = new Category();
        second.setId(3L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void category_equalsWithSameInstance_isTrue() {
        Category category = new Category();

        assertTrue(category.equals(category));
    }

    @Test
    void category_equalsWithNullId_isFalse() {
        Category first = new Category();
        Category second = new Category();

        assertFalse(first.equals(second));
    }

    @Test
    void productOption_equalsWithSameId_isTrue() {
        ProductOption first = new ProductOption();
        first.setId(4L);
        ProductOption second = new ProductOption();
        second.setId(4L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productOption_equalsWithNullId_isFalse() {
        ProductOption first = new ProductOption();
        ProductOption second = new ProductOption();

        assertFalse(first.equals(second));
    }

    @Test
    void productOption_equalsWithSameInstance_isTrue() {
        ProductOption option = new ProductOption();

        assertTrue(option.equals(option));
    }

    @Test
    void productOption_equalsWithDifferentType_isFalse() {
        ProductOption option = new ProductOption();
        option.setId(4L);

        assertFalse(option.equals("not-a-product-option"));
    }

    @Test
    void productOptionValue_equalsWithSameId_isTrue() {
        ProductOptionValue first = new ProductOptionValue();
        first.setId(5L);
        ProductOptionValue second = new ProductOptionValue();
        second.setId(5L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productOptionValue_equalsWithNullId_isFalse() {
        ProductOptionValue first = new ProductOptionValue();
        ProductOptionValue second = new ProductOptionValue();

        assertFalse(first.equals(second));
    }

    @Test
    void productOptionValue_equalsWithSameInstance_isTrue() {
        ProductOptionValue value = new ProductOptionValue();

        assertTrue(value.equals(value));
    }

    @Test
    void productOptionValue_equalsWithDifferentType_isFalse() {
        ProductOptionValue value = new ProductOptionValue();
        value.setId(5L);

        assertFalse(value.equals(123));
    }

    @Test
    void productOptionValue_builderStoresValues() {
        Product product = new Product();
        product.setId(40L);
        ProductOption option = new ProductOption();
        option.setId(41L);

        ProductOptionValue value = ProductOptionValue.builder()
                .id(42L)
                .product(product)
                .productOption(option)
                .displayType("text")
                .displayOrder(3)
                .value("Red")
                .build();

        assertEquals(42L, value.getId());
        assertEquals(40L, value.getProduct().getId());
        assertEquals(41L, value.getProductOption().getId());
        assertEquals("text", value.getDisplayType());
        assertEquals(3, value.getDisplayOrder());
        assertEquals("Red", value.getValue());
    }

    @Test
    void productOptionCombination_equalsWithSameId_isTrue() {
        ProductOptionCombination first = new ProductOptionCombination();
        first.setId(6L);
        ProductOptionCombination second = new ProductOptionCombination();
        second.setId(6L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productOptionCombination_equalsWithSameInstance_isTrue() {
        ProductOptionCombination combination = new ProductOptionCombination();

        assertTrue(combination.equals(combination));
    }

    @Test
    void productOptionCombination_equalsWithNullId_isFalse() {
        ProductOptionCombination first = new ProductOptionCombination();
        ProductOptionCombination second = new ProductOptionCombination();

        assertFalse(first.equals(second));
    }

    @Test
    void productOptionCombination_builderStoresValues() {
        Product product = new Product();
        product.setId(50L);
        ProductOption option = new ProductOption();
        option.setId(51L);

        ProductOptionCombination combination = ProductOptionCombination.builder()
                .id(52L)
                .product(product)
                .productOption(option)
                .displayOrder(1)
                .value("Blue")
                .build();

        assertEquals(52L, combination.getId());
        assertEquals(50L, combination.getProduct().getId());
        assertEquals(51L, combination.getProductOption().getId());
        assertEquals(1, combination.getDisplayOrder());
        assertEquals("Blue", combination.getValue());
    }

    @Test
    void productRelated_equalsWithSameId_isTrue() {
        ProductRelated first = new ProductRelated();
        first.setId(7L);
        ProductRelated second = new ProductRelated();
        second.setId(7L);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void productRelated_equalsWithNullId_isFalse() {
        ProductRelated first = new ProductRelated();
        ProductRelated second = new ProductRelated();

        assertFalse(first.equals(second));
    }

    @Test
    void productRelated_equalsWithSameInstance_isTrue() {
        ProductRelated related = new ProductRelated();

        assertTrue(related.equals(related));
    }

    @Test
    void productRelated_equalsWithDifferentType_isFalse() {
        ProductRelated related = new ProductRelated();
        related.setId(7L);

        assertFalse(related.equals(new Object()));
    }

    @Test
    void productCategory_builderStoresValues() {
        ProductCategory productCategory = ProductCategory.builder()
                .id(9L)
                .displayOrder(2)
                .isFeaturedProduct(true)
                .build();

        assertEquals(9L, productCategory.getId());
        assertEquals(2, productCategory.getDisplayOrder());
        assertTrue(productCategory.isFeaturedProduct());
    }

    @Test
    void productImage_builderStoresValues() {
        ProductImage productImage = ProductImage.builder()
                .id(10L)
                .imageId(20L)
                .build();

        assertEquals(10L, productImage.getId());
        assertEquals(20L, productImage.getImageId());
    }

    @Test
    void productRelated_builderStoresValues() {
        Product product = new Product();
        product.setId(30L);
        Product related = new Product();
        related.setId(31L);

        ProductRelated relation = ProductRelated.builder()
                .product(product)
                .relatedProduct(related)
                .build();

        assertEquals(30L, relation.getProduct().getId());
        assertEquals(31L, relation.getRelatedProduct().getId());
    }
}
