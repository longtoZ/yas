package com.yas.inventory.viewmodel.stock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StockQuantityVmTest {

    @Test
    void testStockQuantityVmConstructor_withAllParameters_createsObject() {
        Long stockId = 1L;
        Long quantity = 50L;
        String note = "Stock replenishment";

        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, quantity, note);

        assertThat(stockQuantityVm.stockId()).isEqualTo(stockId);
        assertThat(stockQuantityVm.quantity()).isEqualTo(quantity);
        assertThat(stockQuantityVm.note()).isEqualTo(note);
    }

    @Test
    void testStockQuantityVm_withNullQuantity_storesNull() {
        Long stockId = 1L;
        String note = "No adjustment";

        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, null, note);

        assertThat(stockQuantityVm.stockId()).isEqualTo(stockId);
        assertThat(stockQuantityVm.quantity()).isNull();
        assertThat(stockQuantityVm.note()).isEqualTo(note);
    }

    @Test
    void testStockQuantityVm_withNegativeQuantity_storesNegativeValue() {
        Long stockId = 2L;
        Long quantity = -25L;
        String note = "Damage adjustment";

        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, quantity, note);

        assertThat(stockQuantityVm.quantity()).isEqualTo(-25L);
    }

    @Test
    void testStockQuantityVm_withZeroQuantity_storesZero() {
        Long stockId = 3L;
        Long quantity = 0L;
        String note = "Inventory check";

        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, quantity, note);

        assertThat(stockQuantityVm.quantity()).isZero();
    }

    @Test
    void testStockQuantityVm_recordEquals_identicalValues() {
        StockQuantityVm stock1 = new StockQuantityVm(1L, 50L, "Note");
        StockQuantityVm stock2 = new StockQuantityVm(1L, 50L, "Note");

        assertThat(stock1).isEqualTo(stock2);
    }

    @Test
    void testStockQuantityVm_recordHashCode_sameForIdenticalValues() {
        StockQuantityVm stock1 = new StockQuantityVm(1L, 50L, "Note");
        StockQuantityVm stock2 = new StockQuantityVm(1L, 50L, "Note");

        assertThat(stock1.hashCode()).isEqualTo(stock2.hashCode());
    }

    @Test
    void testStockQuantityVm_notEqual_differentStockId() {
        StockQuantityVm stock1 = new StockQuantityVm(1L, 50L, "Note");
        StockQuantityVm stock2 = new StockQuantityVm(2L, 50L, "Note");

        assertThat(stock1).isNotEqualTo(stock2);
    }

    @Test
    void testStockQuantityVm_notEqual_differentQuantity() {
        StockQuantityVm stock1 = new StockQuantityVm(1L, 50L, "Note");
        StockQuantityVm stock2 = new StockQuantityVm(1L, 100L, "Note");

        assertThat(stock1).isNotEqualTo(stock2);
    }

    @Test
    void testStockQuantityVm_toString_producesValidString() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, 50L, "Test Note");

        String toStringResult = stockQuantityVm.toString();

        assertThat(toStringResult).isNotNull();
        assertThat(toStringResult).contains("1");
        assertThat(toStringResult).contains("50");
    }
}
