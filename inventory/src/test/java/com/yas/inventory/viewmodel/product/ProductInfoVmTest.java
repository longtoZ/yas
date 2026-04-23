package com.yas.inventory.viewmodel.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import org.junit.jupiter.api.Test;

class ProductInfoVmTest {

    @Test
    void testProductInfoVmConstructor_withAllParameters_createsObject() {
        long id = 1L;
        String name = "Product Name";
        String sku = "SKU-001";
        boolean existInWh = true;

        ProductInfoVm productInfoVm = new ProductInfoVm(id, name, sku, existInWh);

        assertThat(productInfoVm.id()).isEqualTo(id);
        assertThat(productInfoVm.name()).isEqualTo(name);
        assertThat(productInfoVm.sku()).isEqualTo(sku);
        assertThat(productInfoVm.existInWh()).isTrue();
    }

    @Test
    void testProductInfoVm_whenExistInWhFalse_storesFalse() {
        long id = 2L;
        String name = "Another Product";
        String sku = "SKU-002";
        boolean existInWh = false;

        ProductInfoVm productInfoVm = new ProductInfoVm(id, name, sku, existInWh);

        assertThat(productInfoVm.existInWh()).isFalse();
    }

    @Test
    void testProductInfoVm_recordEquals_identicalValues() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(1L, "Product", "SKU-001", true);

        assertThat(product1).isEqualTo(product2);
    }

    @Test
    void testProductInfoVm_recordHashCode_sameForIdenticalValues() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(1L, "Product", "SKU-001", true);

        assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
    }

    @Test
    void testProductInfoVm_notEqual_differentId() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(2L, "Product", "SKU-001", true);

        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void testProductInfoVm_notEqual_differentName() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product 1", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(1L, "Product 2", "SKU-001", true);

        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void testProductInfoVm_notEqual_differentSku() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(1L, "Product", "SKU-002", true);

        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void testProductInfoVm_notEqual_differentExistInWh() {
        ProductInfoVm product1 = new ProductInfoVm(1L, "Product", "SKU-001", true);
        ProductInfoVm product2 = new ProductInfoVm(1L, "Product", "SKU-001", false);

        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void testProductInfoVm_toString_producesValidString() {
        ProductInfoVm productInfoVm = new ProductInfoVm(1L, "Test Product", "TEST-SKU", true);

        String toStringResult = productInfoVm.toString();

        assertThat(toStringResult).isNotNull();
        assertThat(toStringResult).contains("1");
        assertThat(toStringResult).contains("Test Product");
        assertThat(toStringResult).contains("TEST-SKU");
    }

    @Test
    void testProductInfoVm_withLongProductName_storesCorrectly() {
        long id = 100L;
        String longName = "Very Long Product Name With Multiple Words And Special Characters!@#";
        String sku = "LONG-SKU-123";
        boolean existInWh = true;

        ProductInfoVm productInfoVm = new ProductInfoVm(id, longName, sku, existInWh);

        assertThat(productInfoVm.name()).isEqualTo(longName);
        assertThat(productInfoVm.id()).isEqualTo(id);
    }
}
