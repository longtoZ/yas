package com.yas.recommendation.vector.product.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.recommendation.vector.product.store.ProductVectorRepository;
import org.junit.jupiter.api.Test;

class ProductVectorSyncServiceTest {

    @Test
    void createProductVector_onlyWhenPublished() {
        ProductVectorRepository repository = mock(ProductVectorRepository.class);
        ProductVectorSyncService service = new ProductVectorSyncService(repository);
        Product published = Product.builder().id(1L).isPublished(true).build();
        Product unpublished = Product.builder().id(2L).isPublished(false).build();

        service.createProductVector(published);
        service.createProductVector(unpublished);

        verify(repository).add(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateProductVector_addsOrDeletesBasedOnPublished() {
        ProductVectorRepository repository = mock(ProductVectorRepository.class);
        ProductVectorSyncService service = new ProductVectorSyncService(repository);
        Product published = Product.builder().id(1L).isPublished(true).build();
        Product unpublished = Product.builder().id(2L).isPublished(false).build();

        service.updateProductVector(published);
        service.updateProductVector(unpublished);

        verify(repository).update(1L);
        verify(repository).delete(2L);
    }

    @Test
    void deleteProductVector_deletesById() {
        ProductVectorRepository repository = mock(ProductVectorRepository.class);
        ProductVectorSyncService service = new ProductVectorSyncService(repository);

        service.deleteProductVector(3L);

        verify(repository).delete(3L);
    }
}
