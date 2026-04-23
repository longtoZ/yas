package com.yas.inventory.service;

import static com.yas.inventory.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockHistoryServiceTest {

    private StockHistoryRepository stockHistoryRepository;
    private ProductService productService;
    private StockHistoryService stockHistoryService;

    @BeforeEach
    void setUp() {
        stockHistoryRepository = mock(StockHistoryRepository.class);
        productService = mock(ProductService.class);
        stockHistoryService = new StockHistoryService(stockHistoryRepository, productService);
        setUpSecurityContext("test");
    }

    @Test
    void testCreateStockHistories_whenValidInput_savesHistories() {
        Long productId = 1L;
        Long stockId = 1L;
        Long warehouseId = 1L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(stockId)
                .productId(productId)
                .quantity(100L)
                .warehouse(warehouse)
                .build();

        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, 50L, "Added 50 units");

        when(stockHistoryRepository.saveAll(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockHistoryService.createStockHistories(List.of(stock),
                List.of(stockQuantityVm)));

        verify(stockHistoryRepository, times(1)).saveAll(any());
    }

    @Test
    void testCreateStockHistories_whenMultipleStocks_savesAllHistories() {
        Long warehouseId = 1L;
        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();

        Stock stock1 = Stock.builder()
                .id(1L)
                .productId(1L)
                .quantity(100L)
                .warehouse(warehouse)
                .build();
        Stock stock2 = Stock.builder()
                .id(2L)
                .productId(2L)
                .quantity(200L)
                .warehouse(warehouse)
                .build();

        StockQuantityVm stockQuantityVm1 = new StockQuantityVm(1L, 50L, "Restock 1");
        StockQuantityVm stockQuantityVm2 = new StockQuantityVm(2L, 100L, "Restock 2");

        when(stockHistoryRepository.saveAll(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockHistoryService.createStockHistories(List.of(stock1, stock2),
                List.of(stockQuantityVm1, stockQuantityVm2)));

        verify(stockHistoryRepository, times(1)).saveAll(any());
    }

    @Test
    void testCreateStockHistories_whenStockQuantityNotFound_skipsHistory() {
        Long warehouseId = 1L;
        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();

        Stock stock = Stock.builder()
                .id(1L)
                .productId(1L)
                .quantity(100L)
                .warehouse(warehouse)
                .build();

        // No matching StockQuantityVm for this stock
        StockQuantityVm stockQuantityVm = new StockQuantityVm(2L, 50L, "Different stock");

        when(stockHistoryRepository.saveAll(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockHistoryService.createStockHistories(List.of(stock),
                List.of(stockQuantityVm)));

        verify(stockHistoryRepository, times(1)).saveAll(any());
    }

    @Test
    void testCreateStockHistories_whenEmptyStocks_savesNothing() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, 50L, "Test");

        when(stockHistoryRepository.saveAll(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockHistoryService.createStockHistories(List.of(),
                List.of(stockQuantityVm)));

        verify(stockHistoryRepository, times(1)).saveAll(any());
    }

    @Test
    void testGetStockHistories_whenValidInput_returnHistories() {
        Long productId = 1L;
        Long warehouseId = 1L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        StockHistory history = StockHistory.builder()
                .id(1L)
                .productId(productId)
                .adjustedQuantity(50L)
                .note("Stock added")
                .warehouse(warehouse)
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(productId, "Test Product", "TEST-SKU", true);

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of(history));
        when(productService.getProduct(productId)).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(productId, warehouseId);

        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        verify(stockHistoryRepository, times(1))
                .findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId);
    }

    @Test
    void testGetStockHistories_whenNoHistories_returnEmptyList() {
        Long productId = 1L;
        Long warehouseId = 1L;

        ProductInfoVm productInfoVm = new ProductInfoVm(productId, "Test Product", "TEST-SKU", true);

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of());
        when(productService.getProduct(productId)).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(productId, warehouseId);

        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
    }

    @Test
    void testGetStockHistories_whenMultipleHistories_returnAllInOrder() {
        Long productId = 1L;
        Long warehouseId = 1L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        StockHistory history1 = StockHistory.builder()
                .id(1L)
                .productId(productId)
                .adjustedQuantity(50L)
                .note("First adjustment")
                .warehouse(warehouse)
                .build();
        StockHistory history2 = StockHistory.builder()
                .id(2L)
                .productId(productId)
                .adjustedQuantity(-10L)
                .note("Second adjustment")
                .warehouse(warehouse)
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(productId, "Test Product", "TEST-SKU", true);

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of(history1, history2));
        when(productService.getProduct(productId)).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(productId, warehouseId);

        assertThat(result.data()).hasSize(2);
    }

    @Test
    void testGetStockHistories_whenProductNotFound_propagatesException() {
        Long productId = 1L;
        Long warehouseId = 1L;

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of());
        when(productService.getProduct(productId)).thenThrow(new NotFoundException("Product not found", productId));

        assertThrows(NotFoundException.class,
                () -> stockHistoryService.getStockHistories(productId, warehouseId));
    }
}
