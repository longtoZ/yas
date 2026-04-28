package com.yas.inventory.service;

import static com.yas.inventory.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressPostVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class WarehouseServiceTest {

    private WarehouseRepository warehouseRepository;
    private StockRepository stockRepository;
    private ProductService productService;
    private LocationService locationService;
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        stockRepository = mock(StockRepository.class);
        productService = mock(ProductService.class);
        locationService = mock(LocationService.class);
        warehouseService = new WarehouseService(warehouseRepository, stockRepository, productService,
                locationService);
        setUpSecurityContext("test");
    }

    @Test
    void testFindAllWarehouses_whenWarehouses_returnList() {
        Warehouse warehouse1 = Warehouse.builder().id(1L).name("Warehouse 1").addressId(1L).build();
        Warehouse warehouse2 = Warehouse.builder().id(2L).name("Warehouse 2").addressId(2L).build();

        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse1, warehouse2));

        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

        assertThat(result).hasSize(2);
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void testFindAllWarehouses_whenNoWarehouses_returnEmptyList() {
        when(warehouseRepository.findAll()).thenReturn(List.of());

        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetProductWarehouse_whenProductsExist_returnProductsWithInStockFlag() {
        Long warehouseId = 1L;
        String productName = "Product 1";
        String productSku = "SKU-001";
        List<Long> productIds = List.of(1L, 2L);

        ProductInfoVm product1 = new ProductInfoVm(1L, productName, productSku, true);
        ProductInfoVm product2 = new ProductInfoVm(2L, "Product 2", "SKU-002", true);

        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(productIds);
        when(productService.filterProducts(productName, productSku, productIds, FilterExistInWhSelection.YES))
                .thenReturn(List.of(product1, product2));

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(warehouseId, productName, productSku,
                FilterExistInWhSelection.YES);

        assertThat(result).hasSize(2);
        verify(productService, times(1)).filterProducts(productName, productSku, productIds,
                FilterExistInWhSelection.YES);
    }

    @Test
    void testGetProductWarehouse_whenNoProductsInWarehouse_returnEmpty() {
        Long warehouseId = 1L;

        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(List.of());
        when(productService.filterProducts("", "", List.of(), FilterExistInWhSelection.YES))
                .thenReturn(List.of());

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(warehouseId, "", "",
                FilterExistInWhSelection.YES);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindById_whenWarehouseExists_returnDetail() {
        Long warehouseId = 1L;
        Long addressId = 1L;

        Warehouse warehouse = Warehouse.builder()
                .id(warehouseId)
                .name("Main Warehouse")
                .addressId(addressId)
                .build();

        AddressDetailVm addressDetailVm = AddressDetailVm.builder()
                .id(addressId)
                .contactName("John Doe")
                .phone("123-456-7890")
                .addressLine1("123 Main St")
                .city("Metropolis")
                .zipCode("12345")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(locationService.getAddressById(addressId)).thenReturn(addressDetailVm);

        WarehouseDetailVm result = warehouseService.findById(warehouseId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(warehouseId);
        assertThat(result.name()).isEqualTo("Main Warehouse");
        verify(warehouseRepository, times(1)).findById(warehouseId);
    }

    @Test
    void testFindById_whenWarehouseNotFound_throwNotFoundException() {
        Long warehouseId = 999L;

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.findById(warehouseId));
    }

    @Test
    void testCreate_whenValidInput_saveWarehouse() {
        Long addressId = 1L;

        WarehousePostVm warehousePostVm = WarehousePostVm.builder()
                .name("New Warehouse")
                .contactName("John Smith")
                .phone("555-1234")
                .addressLine1("789 Oak St")
                .addressLine2("Suite 1")
                .city("Smalltown")
                .zipCode("67890")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        AddressVm addressVm = AddressVm.builder()
                .id(addressId)
                .contactName("John Smith")
                .phone("555-1234")
                .addressLine1("789 Oak St")
                .city("Smalltown")
                .zipCode("67890")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        Warehouse savedWarehouse = Warehouse.builder()
                .id(1L)
                .name("New Warehouse")
                .addressId(addressId)
                .build();

        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(false);
        when(locationService.createAddress(any())).thenReturn(addressVm);
        when(warehouseRepository.save(any())).thenReturn(savedWarehouse);

        Warehouse result = warehouseService.create(warehousePostVm);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Warehouse");
        verify(warehouseRepository, times(1)).save(any());
    }

    @Test
    void testCreate_whenNameDuplicate_throwDuplicatedException() {
        WarehousePostVm warehousePostVm = WarehousePostVm.builder()
                .name("Existing Warehouse")
                .contactName("John Smith")
                .phone("555-1234")
                .addressLine1("789 Oak St")
                .addressLine2("Suite 1")
                .city("Smalltown")
                .zipCode("67890")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        when(warehouseRepository.existsByName("Existing Warehouse")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.create(warehousePostVm));
    }

    @Test
    void testUpdate_whenWarehouseExists_updateSuccessfully() {
        Long warehouseId = 1L;
        Long addressId = 1L;

        Warehouse warehouse = Warehouse.builder()
                .id(warehouseId)
                .name("Old Warehouse")
                .addressId(addressId)
                .build();

        WarehousePostVm warehousePostVm = WarehousePostVm.builder()
                .name("Updated Warehouse")
                .contactName("Jane Doe")
                .phone("555-5678")
                .addressLine1("456 Elm St")
                .addressLine2("Suite 2")
                .city("Metropolis")
                .zipCode("54321")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId("Updated Warehouse", warehouseId))
                .thenReturn(false);
        when(warehouseRepository.save(any())).thenReturn(warehouse);

        assertDoesNotThrow(() -> warehouseService.update(warehousePostVm, warehouseId));

        verify(warehouseRepository, times(1)).save(any());
        verify(locationService, times(1)).updateAddress(eq(addressId), any());
    }

    @Test
    void testUpdate_whenWarehouseNotFound_throwNotFoundException() {
        Long warehouseId = 999L;
        WarehousePostVm warehousePostVm = WarehousePostVm.builder()
                .name("Updated Warehouse")
                .contactName("Jane Doe")
                .phone("555-5678")
                .addressLine1("456 Elm St")
                .addressLine2("Suite 2")
                .city("Metropolis")
                .zipCode("54321")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.update(warehousePostVm, warehouseId));
    }

    @Test
    void testUpdate_whenNameDuplicate_throwDuplicatedException() {
        Long warehouseId = 1L;
        Long addressId = 1L;

        Warehouse warehouse = Warehouse.builder()
                .id(warehouseId)
                .name("Old Warehouse")
                .addressId(addressId)
                .build();

        WarehousePostVm warehousePostVm = WarehousePostVm.builder()
                .name("Duplicate Name")
                .contactName("Jane Doe")
                .phone("555-5678")
                .addressLine1("456 Elm St")
                .addressLine2("Suite 2")
                .city("Metropolis")
                .zipCode("54321")
                .districtId(1L)
                .stateOrProvinceId(2L)
                .countryId(3L)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId("Duplicate Name", warehouseId))
                .thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.update(warehousePostVm, warehouseId));
    }

    @Test
    void testDelete_whenWarehouseExists_deleteSuccessfully() {
        Long warehouseId = 1L;
        Long addressId = 1L;

        Warehouse warehouse = Warehouse.builder()
                .id(warehouseId)
                .name("Warehouse to Delete")
                .addressId(addressId)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        assertDoesNotThrow(() -> warehouseService.delete(warehouseId));

        verify(warehouseRepository, times(1)).deleteById(warehouseId);
        verify(locationService, times(1)).deleteAddress(addressId);
    }

    @Test
    void testDelete_whenWarehouseNotFound_throwNotFoundException() {
        Long warehouseId = 999L;

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.delete(warehouseId));
    }

    @Test
    void testGetPageableWarehouses_whenWarehouses_returnPagedResult() {
        int pageNo = 0;
        int pageSize = 10;

        Warehouse warehouse1 = Warehouse.builder().id(1L).name("Warehouse 1").addressId(1L).build();
        Warehouse warehouse2 = Warehouse.builder().id(2L).name("Warehouse 2").addressId(2L).build();

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Warehouse> warehousePage = new PageImpl<>(List.of(warehouse1, warehouse2), pageable, 2);

        when(warehouseRepository.findAll(pageable)).thenReturn(warehousePage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(pageNo, pageSize);

        assertThat(result).isNotNull();
        assertThat(result.warehouseContent()).hasSize(2);
        assertThat(result.pageNo()).isEqualTo(pageNo);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        verify(warehouseRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetPageableWarehouses_whenNoWarehouses_returnEmptyPage() {
        int pageNo = 0;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Warehouse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(warehouseRepository.findAll(pageable)).thenReturn(emptyPage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(pageNo, pageSize);

        assertThat(result.warehouseContent()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void testGetPageableWarehouses_withMultiplePages_returnCorrectPage() {
        int pageNo = 1;
        int pageSize = 5;

        Warehouse warehouse1 = Warehouse.builder().id(6L).name("Warehouse 6").addressId(6L).build();
        Warehouse warehouse2 = Warehouse.builder().id(7L).name("Warehouse 7").addressId(7L).build();

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Warehouse> warehousePage = new PageImpl<>(List.of(warehouse1, warehouse2), pageable, 12);

        when(warehouseRepository.findAll(pageable)).thenReturn(warehousePage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(pageNo, pageSize);

        assertThat(result.pageNo()).isEqualTo(pageNo);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.isLast()).isFalse();
    }
}
