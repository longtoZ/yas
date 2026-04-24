package com.yas.recommendation.kafka.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.yas.commonlibrary.kafka.cdc.message.Operation;
import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import com.yas.recommendation.vector.product.service.ProductVectorSyncService;
import org.junit.jupiter.api.Test;

class ProductSyncServiceTest {

    @Test
    void sync_deletesOnNullMessage() {
        ProductVectorSyncService vectorSyncService = mock(ProductVectorSyncService.class);
        ProductSyncService service = new ProductSyncService(vectorSyncService);
        ProductMsgKey key = ProductMsgKey.builder().id(10L).build();

        service.sync(key, null);

        verify(vectorSyncService).deleteProductVector(10L);
        verifyNoMoreInteractions(vectorSyncService);
    }

    @Test
    void sync_deletesOnDeleteOperation() {
        ProductVectorSyncService vectorSyncService = mock(ProductVectorSyncService.class);
        ProductSyncService service = new ProductSyncService(vectorSyncService);
        ProductMsgKey key = ProductMsgKey.builder().id(11L).build();
        ProductCdcMessage message = ProductCdcMessage.builder()
                .op(Operation.DELETE)
                .build();

        service.sync(key, message);

        verify(vectorSyncService).deleteProductVector(11L);
        verifyNoMoreInteractions(vectorSyncService);
    }

    @Test
    void sync_createsOnCreateOrRead() {
        ProductVectorSyncService vectorSyncService = mock(ProductVectorSyncService.class);
        ProductSyncService service = new ProductSyncService(vectorSyncService);
        Product product = Product.builder().id(1L).isPublished(true).build();
        ProductCdcMessage createMessage = ProductCdcMessage.builder()
                .op(Operation.CREATE)
                .after(product)
                .build();
        ProductCdcMessage readMessage = ProductCdcMessage.builder()
                .op(Operation.READ)
                .after(product)
                .build();

        service.sync(ProductMsgKey.builder().id(1L).build(), createMessage);
        service.sync(ProductMsgKey.builder().id(1L).build(), readMessage);

        verify(vectorSyncService, times(2)).createProductVector(product);
    }

    @Test
    void sync_updatesOnUpdate() {
        ProductVectorSyncService vectorSyncService = mock(ProductVectorSyncService.class);
        ProductSyncService service = new ProductSyncService(vectorSyncService);
        Product product = Product.builder().id(2L).isPublished(true).build();
        ProductCdcMessage updateMessage = ProductCdcMessage.builder()
                .op(Operation.UPDATE)
                .after(product)
                .build();

        service.sync(ProductMsgKey.builder().id(2L).build(), updateMessage);

        verify(vectorSyncService).updateProductVector(product);
        verifyNoMoreInteractions(vectorSyncService);
    }

    @Test
    void sync_skipsWhenAfterIsNull() {
        ProductVectorSyncService vectorSyncService = mock(ProductVectorSyncService.class);
        ProductSyncService service = new ProductSyncService(vectorSyncService);
        ProductCdcMessage message = ProductCdcMessage.builder()
                .op(Operation.UPDATE)
                .after(null)
                .build();

        service.sync(ProductMsgKey.builder().id(3L).build(), message);

        verifyNoMoreInteractions(vectorSyncService);
    }
}
