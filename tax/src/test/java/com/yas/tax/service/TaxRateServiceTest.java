package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TaxRateServiceTest {

    @Mock
    private TaxRateRepository taxRateRepository;

    @Mock
    private TaxClassRepository taxClassRepository;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private TaxRateService taxRateService;

    @Test
    void createTaxRate_WhenTaxClassMissing_ThrowNotFoundException() {
        TaxRatePostVm request = new TaxRatePostVm(0.1, "70000", 2L, 10L, 84L);
        when(taxClassRepository.existsById(2L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.createTaxRate(request));
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void createTaxRate_WhenRequestValid_SaveTaxRate() {
        TaxClass taxClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate saved = TaxRate.builder()
            .id(5L)
            .rate(0.1)
            .zipCode("70000")
            .taxClass(taxClass)
            .stateOrProvinceId(10L)
            .countryId(84L)
            .build();
        TaxRatePostVm request = new TaxRatePostVm(0.1, "70000", 2L, 10L, 84L);

        when(taxClassRepository.existsById(2L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(2L)).thenReturn(taxClass);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(saved);

        TaxRate result = taxRateService.createTaxRate(request);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void updateTaxRate_WhenTaxRateMissing_ThrowNotFoundException() {
        TaxRatePostVm request = new TaxRatePostVm(0.15, "70000", 2L, 10L, 84L);
        when(taxRateRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxRateService.updateTaxRate(request, 8L));
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void updateTaxRate_WhenTaxClassMissing_ThrowNotFoundException() {
        TaxClass currentClass = TaxClass.builder().id(1L).name("Old").build();
        TaxRate existing = TaxRate.builder().id(8L).taxClass(currentClass).rate(0.05).countryId(84L).build();
        TaxRatePostVm request = new TaxRatePostVm(0.15, "70000", 2L, 10L, 84L);
        when(taxRateRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(taxClassRepository.existsById(2L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.updateTaxRate(request, 8L));
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void updateTaxRate_WhenRequestValid_UpdateAndSaveTaxRate() {
        TaxClass oldClass = TaxClass.builder().id(1L).name("Old").build();
        TaxClass newClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate existing = TaxRate.builder()
            .id(8L)
            .rate(0.05)
            .zipCode("old")
            .taxClass(oldClass)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();
        TaxRatePostVm request = new TaxRatePostVm(0.15, "70000", 2L, 10L, 84L);

        when(taxRateRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(taxClassRepository.existsById(2L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(2L)).thenReturn(newClass);

        taxRateService.updateTaxRate(request, 8L);

        assertThat(existing.getRate()).isEqualTo(0.15);
        assertThat(existing.getZipCode()).isEqualTo("70000");
        assertThat(existing.getTaxClass()).isEqualTo(newClass);
        assertThat(existing.getStateOrProvinceId()).isEqualTo(10L);
        assertThat(existing.getCountryId()).isEqualTo(84L);
        verify(taxRateRepository).save(existing);
    }

    @Test
    void delete_WhenTaxRateMissing_ThrowNotFoundException() {
        when(taxRateRepository.existsById(11L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.delete(11L));
        verify(taxRateRepository, never()).deleteById(11L);
    }

    @Test
    void delete_WhenTaxRateExists_DeleteById() {
        when(taxRateRepository.existsById(11L)).thenReturn(true);

        taxRateService.delete(11L);

        verify(taxRateRepository).deleteById(11L);
    }

    @Test
    void findById_WhenTaxRateExists_ReturnMappedVm() {
        TaxClass taxClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate taxRate = TaxRate.builder()
            .id(1L)
            .rate(0.1)
            .zipCode("70000")
            .taxClass(taxClass)
            .stateOrProvinceId(10L)
            .countryId(84L)
            .build();
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(1L);

        assertThat(result).isEqualTo(TaxRateVm.fromModel(taxRate));
    }

    @Test
    void findById_WhenTaxRateMissing_ThrowNotFoundException() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxRateService.findById(1L));
    }

    @Test
    void getPageableTaxRates_WhenRatesExist_ReturnEnrichedDetails() {
        TaxClass taxClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate taxRate = TaxRate.builder()
            .id(1L)
            .rate(0.1)
            .zipCode("70000")
            .taxClass(taxClass)
            .stateOrProvinceId(10L)
            .countryId(84L)
            .build();
        PageImpl<TaxRate> page = new PageImpl<>(List.of(taxRate), PageRequest.of(0, 5), 1);

        when(taxRateRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);
        when(locationService.getStateOrProvinceAndCountryNames(List.of(10L)))
            .thenReturn(List.of(new StateOrProvinceAndCountryGetNameVm(10L, "Ho Chi Minh", "Vietnam")));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 5);

        assertThat(result.taxRateGetDetailContent()).containsExactly(
            new TaxRateGetDetailVm(1L, 0.1, "70000", "Standard", "Ho Chi Minh", "Vietnam")
        );
        assertThat(result.pageNo()).isZero();
        assertThat(result.pageSize()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getPageableTaxRates_WhenNoRatesExist_ReturnEmptyDetailList() {
        PageImpl<TaxRate> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(taxRateRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 5);

        assertThat(result.taxRateGetDetailContent()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        verify(locationService, never()).getStateOrProvinceAndCountryNames(any());
    }

    @Test
    void getTaxPercent_WhenRepositoryHasValue_ReturnThatValue() {
        when(taxRateRepository.getTaxPercent(84L, 10L, "70000", 2L)).thenReturn(0.1);

        double result = taxRateService.getTaxPercent(2L, 84L, 10L, "70000");

        assertThat(result).isEqualTo(0.1);
    }

    @Test
    void getTaxPercent_WhenRepositoryReturnsNull_ReturnZero() {
        when(taxRateRepository.getTaxPercent(84L, 10L, "70000", 2L)).thenReturn(null);

        double result = taxRateService.getTaxPercent(2L, 84L, 10L, "70000");

        assertThat(result).isZero();
    }

    @Test
    void getBulkTaxRate_WhenRatesExist_ReturnMappedRates() {
        TaxClass taxClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate taxRate = TaxRate.builder()
            .id(1L)
            .rate(0.1)
            .zipCode("70000")
            .taxClass(taxClass)
            .stateOrProvinceId(10L)
            .countryId(84L)
            .build();
        when(taxRateRepository.getBatchTaxRates(84L, 10L, "70000", java.util.Set.of(2L, 3L)))
            .thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(2L, 3L), 84L, 10L, "70000");

        assertThat(result).containsExactly(TaxRateVm.fromModel(taxRate));
        verify(taxRateRepository).getBatchTaxRates(84L, 10L, "70000", java.util.Set.of(2L, 3L));
    }
}
