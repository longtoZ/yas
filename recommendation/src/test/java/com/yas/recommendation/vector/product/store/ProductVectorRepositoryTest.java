package com.yas.recommendation.vector.product.store;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.recommendation.service.ProductService;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;

class ProductVectorRepositoryTest {

    private VectorStore vectorStore;
    private ProductService productService;
    private ProductVectorRepository productVectorRepository;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        productService = mock(ProductService.class);
        productVectorRepository = new ProductVectorRepository(vectorStore, productService);
    }

    @Test
    void getEntity_shouldReturnProductDetailVm() {
        long productId = 1L;
        ProductDetailVm expectedVm = new ProductDetailVm(
                1L, "Name", "Short", "Desc", "Spec", "SKU", "GTIN", "Slug",
                true, true, true, true, true, 10.0, 1L, null, "MetaTitle", "MetaKeyword",
                "MetaDesc", 1L, "Brand", null, null, null, null);

        when(productService.getProductDetail(productId)).thenReturn(expectedVm);

        ProductDetailVm actualVm = productVectorRepository.getEntity(productId);

        assertEquals(expectedVm, actualVm);
    }
}
