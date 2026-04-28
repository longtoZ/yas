package com.yas.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.recommendation.configuration.RecommendationConfig;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class ProductServiceTest {

    private RestClient restClient;
    private RecommendationConfig config;
    private ProductService productService;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        config = mock(RecommendationConfig.class);
        productService = new ProductService(restClient, config);
        
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
    }

    @Test
    void getProductDetail_shouldReturnProductDetailVm() {
        long productId = 1L;
        String apiUrl = "http://localhost:8080";
        ProductDetailVm expectedVm = new ProductDetailVm(
                1L, "Name", "Short", "Desc", "Spec", "SKU", "GTIN", "Slug",
                true, true, true, true, true, 10.0, 1L, null, "MetaTitle", "MetaKeyword",
                "MetaDesc", 1L, "Brand", null, null, null, null);

        when(config.getApiUrl()).thenReturn(apiUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(expectedVm));

        ProductDetailVm actualVm = productService.getProductDetail(productId);

        assertEquals(expectedVm, actualVm);
    }
}
