package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TaxClassServiceTest {

    @Mock
    private TaxClassRepository taxClassRepository;

    @InjectMocks
    private TaxClassService taxClassService;

    @Test
    void findAllTaxClasses_WhenDataExists_ReturnSortedMappedResult() {
        TaxClass reduced = TaxClass.builder().id(2L).name("Reduced").build();
        TaxClass standard = TaxClass.builder().id(1L).name("Standard").build();
        when(taxClassRepository.findAll(any(Sort.class))).thenReturn(List.of(reduced, standard));

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertThat(result)
            .containsExactly(TaxClassVm.fromModel(reduced), TaxClassVm.fromModel(standard));
        verify(taxClassRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void findById_WhenTaxClassExists_ReturnMappedVm() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Standard").build();
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));

        TaxClassVm result = taxClassService.findById(1L);

        assertThat(result).isEqualTo(TaxClassVm.fromModel(taxClass));
    }

    @Test
    void findById_WhenTaxClassMissing_ThrowNotFoundException() {
        when(taxClassRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.findById(9L));
    }

    @Test
    void create_WhenNameAlreadyExists_ThrowDuplicatedException() {
        TaxClassPostVm request = new TaxClassPostVm("1", "Standard");
        when(taxClassRepository.existsByName("Standard")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.create(request));
        verify(taxClassRepository, never()).save(any(TaxClass.class));
    }

    @Test
    void create_WhenNameAvailable_SaveTaxClass() {
        TaxClassPostVm request = new TaxClassPostVm("1", "Standard");
        TaxClass saved = TaxClass.builder().id(5L).name("Standard").build();
        when(taxClassRepository.existsByName("Standard")).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(saved);

        TaxClass result = taxClassService.create(request);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void update_WhenTaxClassMissing_ThrowNotFoundException() {
        TaxClassPostVm request = new TaxClassPostVm("1", "Updated");
        when(taxClassRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.update(request, 7L));
        verify(taxClassRepository, never()).save(any(TaxClass.class));
    }

    @Test
    void update_WhenDuplicateNameExists_ThrowDuplicatedException() {
        TaxClass existing = TaxClass.builder().id(7L).name("Old").build();
        TaxClassPostVm request = new TaxClassPostVm("1", "Updated");
        when(taxClassRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Updated", 7L)).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.update(request, 7L));
        verify(taxClassRepository, never()).save(any(TaxClass.class));
    }

    @Test
    void update_WhenRequestValid_UpdateAndSaveTaxClass() {
        TaxClass existing = TaxClass.builder().id(7L).name("Old").build();
        TaxClassPostVm request = new TaxClassPostVm("1", "Updated");
        when(taxClassRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Updated", 7L)).thenReturn(false);

        taxClassService.update(request, 7L);

        assertThat(existing.getName()).isEqualTo("Updated");
        verify(taxClassRepository).save(existing);
    }

    @Test
    void delete_WhenTaxClassMissing_ThrowNotFoundException() {
        when(taxClassRepository.existsById(4L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxClassService.delete(4L));
        verify(taxClassRepository, never()).deleteById(4L);
    }

    @Test
    void delete_WhenTaxClassExists_DeleteById() {
        when(taxClassRepository.existsById(4L)).thenReturn(true);

        taxClassService.delete(4L);

        verify(taxClassRepository).deleteById(4L);
    }

    @Test
    void getPageableTaxClasses_WhenPageExists_ReturnMappedPageVm() {
        TaxClass taxClass = TaxClass.builder().id(3L).name("Standard").build();
        PageImpl<TaxClass> page = new PageImpl<>(List.of(taxClass), PageRequest.of(1, 2), 3);
        when(taxClassRepository.findAll(PageRequest.of(1, 2))).thenReturn(page);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(1, 2);

        assertThat(result.taxClassContent()).containsExactly(TaxClassVm.fromModel(taxClass));
        assertThat(result.pageNo()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.isLast()).isTrue();
    }
}
