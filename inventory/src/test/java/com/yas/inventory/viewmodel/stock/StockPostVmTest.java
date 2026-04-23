package com.yas.inventory.viewmodel.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StockPostVmTest {

    @Test
    void testStockPostVmConstructor_withValidParameters_createsObject() {
        Long productId = 1L;
        Long warehouseId = 1L;

        StockPostVm stockPostVm = new StockPostVm(productId, warehouseId);

        assertThat(stockPostVm.productId()).isEqualTo(productId);
        assertThat(stockPostVm.warehouseId()).isEqualTo(warehouseId);
    }

    @Test
    void testStockPostVm_withMultipleValues_storesCorrectly() {
        Long productId = 5L;
        Long warehouseId = 3L;

        StockPostVm stockPostVm = new StockPostVm(productId, warehouseId);

        assertThat(stockPostVm.productId()).isEqualTo(5L);
        assertThat(stockPostVm.warehouseId()).isEqualTo(3L);
    }

    @Test
    void testStockPostVm_recordEquals_identicalValues() {
        StockPostVm stock1 = new StockPostVm(1L, 1L);
        StockPostVm stock2 = new StockPostVm(1L, 1L);

        assertThat(stock1).isEqualTo(stock2);
    }

    @Test
    void testStockPostVm_recordHashCode_sameForIdenticalValues() {
        StockPostVm stock1 = new StockPostVm(1L, 1L);
        StockPostVm stock2 = new StockPostVm(1L, 1L);

        assertThat(stock1.hashCode()).isEqualTo(stock2.hashCode());
    }

    @Test
    void testStockPostVm_notEqual_differentProductId() {
        StockPostVm stock1 = new StockPostVm(1L, 1L);
        StockPostVm stock2 = new StockPostVm(2L, 1L);

        assertThat(stock1).isNotEqualTo(stock2);
    }

    @Test
    void testStockPostVm_notEqual_differentWarehouseId() {
        StockPostVm stock1 = new StockPostVm(1L, 1L);
        StockPostVm stock2 = new StockPostVm(1L, 2L);

        assertThat(stock1).isNotEqualTo(stock2);
    }

    @Test
    void testStockPostVm_toString_producesValidString() {
        StockPostVm stockPostVm = new StockPostVm(1L, 2L);

        String toStringResult = stockPostVm.toString();

        assertThat(toStringResult).isNotNull();
        assertThat(toStringResult).contains("1");
        assertThat(toStringResult).contains("2");
    }
}
