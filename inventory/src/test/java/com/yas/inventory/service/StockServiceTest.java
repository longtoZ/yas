package com.yas.inventory.service;

import static com.yas.inventory.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockServiceTest {

    private WarehouseRepository warehouseRepository;
    private StockRepository stockRepository;
    private ProductService productService;
    private WarehouseService warehouseService;
    private StockHistoryService stockHistoryService;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        stockRepository = mock(StockRepository.class);
        productService = mock(ProductService.class);
        warehouseService = mock(WarehouseService.class);
        stockHistoryService = mock(StockHistoryService.class);
        stockService = new StockService(warehouseRepository, stockRepository, productService,
                warehouseService, stockHistoryService);
        setUpSecurityContext("test");
    }

    @Test
    void testAddProductIntoWarehouse_whenValidInput_saveStocks() {
        Long warehouseId = 1L;
        Long productId = 1L;
        StockPostVm postVm = new StockPostVm(warehouseId, productId);

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Main Warehouse").build();
        ProductInfoVm product = new ProductInfoVm(productId, "Test Product", "TEST-SKU", true);

        when(stockRepository.existsByWarehouseIdAndProductId(warehouseId, productId)).thenReturn(false);
        when(productService.getProduct(productId)).thenReturn(product);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of());

        assertDoesNotThrow(() -> stockService.addProductIntoWarehouse(List.of(postVm)));

        verify(stockRepository, times(1)).saveAll(any());
    }

    @Test
    void testAddProductIntoWarehouse_whenStockExists_throwStockExistingException() {
        Long warehouseId = 1L;
        Long productId = 1L;
        StockPostVm postVm = new StockPostVm(warehouseId, productId);

        when(stockRepository.existsByWarehouseIdAndProductId(warehouseId, productId)).thenReturn(true);

        assertThrows(StockExistingException.class,
                () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void testAddProductIntoWarehouse_whenProductNotFound_throwNotFoundException() {
        Long warehouseId = 1L;
        Long productId = 1L;
        StockPostVm postVm = new StockPostVm(warehouseId, productId);

        when(stockRepository.existsByWarehouseIdAndProductId(warehouseId, productId)).thenReturn(false);
        when(productService.getProduct(productId)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void testAddProductIntoWarehouse_whenWarehouseNotFound_throwNotFoundException() {
        Long warehouseId = 1L;
        Long productId = 1L;
        StockPostVm postVm = new StockPostVm(warehouseId, productId);
        ProductInfoVm product = new ProductInfoVm(productId, "Test Product", "TEST-SKU", true);

        when(stockRepository.existsByWarehouseIdAndProductId(warehouseId, productId)).thenReturn(false);
        when(productService.getProduct(productId)).thenReturn(product);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void testGetStocksByWarehouseIdAndProductNameAndSku_whenValidInput_returnStocks() {
        Long warehouseId = 1L;
        Long productId = 1L;
        String productName = "Product 1";
        String productSku = "SKU-001";

        ProductInfoVm productInfoVm = new ProductInfoVm(productId, productName, productSku, true);
        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(1L)
                .productId(productId)
                .quantity(100L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();

        when(warehouseService.getProductWarehouse(warehouseId, productName, productSku,
                FilterExistInWhSelection.YES))
                .thenReturn(List.of(productInfoVm));
        when(stockRepository.findByWarehouseIdAndProductIdIn(warehouseId, List.of(productId)))
                .thenReturn(List.of(stock));

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId,
                productName, productSku);

        assertThat(result).hasSize(1);
        verify(warehouseService, times(1)).getProductWarehouse(warehouseId, productName, productSku,
                FilterExistInWhSelection.YES);
    }

    @Test
    void testGetStocksByWarehouseIdAndProductNameAndSku_whenNoProducts_returnEmptyList() {
        Long warehouseId = 1L;

        when(warehouseService.getProductWarehouse(warehouseId, "", "", FilterExistInWhSelection.YES))
                .thenReturn(List.of());

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId, "", "");

        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateProductQuantityInStock_whenValidInput_updateStocks() {
        Long stockId = 1L;
        Long warehouseId = 1L;
        Long adjustedQuantity = 50L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(stockId)
                .productId(1L)
                .quantity(100L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();
        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, adjustedQuantity, "Added stock");

        when(stockRepository.findAllById(List.of(stockId))).thenReturn(List.of(stock));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock));

        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(stockQuantityVm));

        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(updateVm));

        verify(stockRepository, times(1)).saveAll(any());
        verify(stockHistoryService, times(1)).createStockHistories(any(), any());
        verify(productService, times(1)).updateProductQuantity(any());
    }

    @Test
    void testUpdateProductQuantityInStock_whenPositiveAdjustment_increasesStock() {
        Long stockId = 1L;
        Long warehouseId = 1L;
        Long adjustedQuantity = 75L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(stockId)
                .productId(1L)
                .quantity(100L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();
        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, adjustedQuantity, "Increase stock");

        when(stockRepository.findAllById(List.of(stockId))).thenReturn(List.of(stock));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock));

        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(stockQuantityVm));

        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(updateVm));

        verify(stockRepository, times(1)).saveAll(any());
    }

    @Test
    void testUpdateProductQuantityInStock_whenQuantityIsNull_defaultToZero() {
        Long stockId = 1L;
        Long warehouseId = 1L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock = Stock.builder()
                .id(stockId)
                .productId(1L)
                .quantity(100L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();
        StockQuantityVm stockQuantityVm = new StockQuantityVm(stockId, null, "No adjustment");

        when(stockRepository.findAllById(List.of(stockId))).thenReturn(List.of(stock));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock));

        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(stockQuantityVm));

        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(updateVm));
    }

    @Test
    void testUpdateProductQuantityInStock_whenMultipleStocks_updateAll() {
        Long stockId1 = 1L;
        Long stockId2 = 2L;
        Long warehouseId = 1L;

        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Test Warehouse").build();
        Stock stock1 = Stock.builder()
                .id(stockId1)
                .productId(1L)
                .quantity(100L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();
        Stock stock2 = Stock.builder()
                .id(stockId2)
                .productId(2L)
                .quantity(200L)
                .reservedQuantity(0L)
                .warehouse(warehouse)
                .build();

        StockQuantityVm stockQuantityVm1 = new StockQuantityVm(stockId1, 50L, "Add to stock 1");
        StockQuantityVm stockQuantityVm2 = new StockQuantityVm(stockId2, 100L, "Add to stock 2");

        when(stockRepository.findAllById(List.of(stockId1, stockId2)))
                .thenReturn(List.of(stock1, stock2));
        when(stockRepository.saveAll(anyList())).thenReturn(List.of(stock1, stock2));

        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(stockQuantityVm1, stockQuantityVm2));

        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(updateVm));

        verify(stockRepository, times(1)).saveAll(any());
    }
}
