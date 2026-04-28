package com.yas.inventory.viewmodel.stock;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import org.junit.jupiter.api.Test;

class StockVmTest {

    @Test
    void testStockVmConstructor_withAllParameters_createsValidObject() {
        Long id = 1L;
        Long productId = 1L;
        String productName = "Product Name";
        String productSku = "SKU-001";
        Long quantity = 100L;
        Long reservedQuantity = 10L;
        Long warehouseId = 1L;

        StockVm stockVm = new StockVm(id, productId, productName, productSku, quantity, reservedQuantity,
                warehouseId);

        assertThat(stockVm.id()).isEqualTo(id);
        assertThat(stockVm.productId()).isEqualTo(productId);
        assertThat(stockVm.productName()).isEqualTo(productName);
        assertThat(stockVm.productSku()).isEqualTo(productSku);
        assertThat(stockVm.quantity()).isEqualTo(quantity);
        assertThat(stockVm.reservedQuantity()).isEqualTo(reservedQuantity);
        assertThat(stockVm.warehouseId()).isEqualTo(warehouseId);
    }

    @Test
    void testStockVm_fromModel_createsFromStockEntity() {
        Long warehouseId = 1L;
        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(1L)
                .productId(1L)
                .quantity(100L)
                .reservedQuantity(10L)
                .warehouse(warehouse)
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(1L, "Test Product", "TEST-SKU", true);

        StockVm result = StockVm.fromModel(stock, productInfoVm);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.productName()).isEqualTo("Test Product");
        assertThat(result.productSku()).isEqualTo("TEST-SKU");
        assertThat(result.quantity()).isEqualTo(100L);
        assertThat(result.reservedQuantity()).isEqualTo(10L);
        assertThat(result.warehouseId()).isEqualTo(warehouseId);
    }

    @Test
    void testStockVm_fromModel_withZeroReservedQuantity_createsCorrectly() {
        Warehouse warehouse = Warehouse.builder().id(1L).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(2L)
                .productId(2L)
                .quantity(50L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(2L, "Another Product", "ANOTHER-SKU", false);

        StockVm result = StockVm.fromModel(stock, productInfoVm);

        assertThat(result.reservedQuantity()).isZero();
        assertThat(result.quantity()).isEqualTo(50L);
    }

    @Test
    void testStockVm_fromModel_withLargeQuantities_handlesCorrectly() {
        Warehouse warehouse = Warehouse.builder().id(5L).name("Large Warehouse").build();
        Stock stock = Stock.builder()
                .id(10L)
                .productId(20L)
                .quantity(1000000L)
                .reservedQuantity(500000L)
                .warehouse(warehouse)
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(20L, "Bulk Product", "BULK-SKU", true);

        StockVm result = StockVm.fromModel(stock, productInfoVm);

        assertThat(result.quantity()).isEqualTo(1000000L);
        assertThat(result.reservedQuantity()).isEqualTo(500000L);
    }

    @Test
    void testStockVm_recordEquals_identicalStocks() {
        StockVm stock1 = new StockVm(1L, 1L, "Product", "SKU", 100L, 10L, 1L);
        StockVm stock2 = new StockVm(1L, 1L, "Product", "SKU", 100L, 10L, 1L);

        assertThat(stock1).isEqualTo(stock2);
    }

    @Test
    void testStockVm_recordHashCode_sameForIdenticalStocks() {
        StockVm stock1 = new StockVm(1L, 1L, "Product", "SKU", 100L, 10L, 1L);
        StockVm stock2 = new StockVm(1L, 1L, "Product", "SKU", 100L, 10L, 1L);

        assertThat(stock1.hashCode()).isEqualTo(stock2.hashCode());
    }
}
