package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    @Test
    void getProductDetailById_whenUnpublished_throwsNotFoundException() {
        Product product = new Product();
        product.setId(1L);
        product.setPublished(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_whenNoRelations_returnsEmptyCollections() {
        Product product = new Product();
        product.setId(10L);
        product.setName("basic-product");
        product.setPublished(true);
        product.setProductCategories(null);
        product.setProductImages(null);
        product.setThumbnailMediaId(null);
        product.setBrand(null);
        product.setAttributeValues(new ArrayList<>());
        product.setHasOptions(false);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(10L);

        assertNotNull(result);
        assertTrue(result.getCategories().isEmpty());
        assertTrue(result.getProductImages().isEmpty());
        assertTrue(result.getVariations().isEmpty());
        assertNull(result.getThumbnail());
        assertNull(result.getBrandId());
        assertNull(result.getBrandName());
    }

    @Test
    void getProductDetailById_whenHasOptions_buildsVariations() {
        Product product = new Product();
        product.setId(100L);
        product.setName("parent");
        product.setPublished(true);
        product.setHasOptions(true);
        product.setAttributeValues(new ArrayList<>());

        Brand brand = new Brand();
        brand.setId(2L);
        brand.setName("BrandA");
        product.setBrand(brand);

        Category category = new Category();
        category.setId(3L);
        category.setName("CategoryA");
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        productCategory.setProduct(product);
        product.setProductCategories(List.of(productCategory));

        Product variation = new Product();
        variation.setId(200L);
        variation.setName("variant-1");
        variation.setSlug("variant-1");
        variation.setSku("sku-1");
        variation.setGtin("gtin-1");
        variation.setPrice(99.99);
        variation.setPublished(true);
        variation.setThumbnailMediaId(10L);

        ProductImage variationImage = new ProductImage();
        variationImage.setImageId(20L);
        variationImage.setProduct(variation);
        variation.setProductImages(List.of(variationImage));

        product.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(7L);
        option.setName("Color");

        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(variation);
        combination.setProductOption(option);
        combination.setValue("Red");

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(10L)).thenReturn(new NoFileMediaVm(10L, "", "", "", "thumb-url"));
        when(mediaService.getMedia(20L)).thenReturn(new NoFileMediaVm(20L, "", "", "", "image-url"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(100L);

        assertEquals(2L, result.getBrandId());
        assertEquals("BrandA", result.getBrandName());
        assertEquals(1, result.getCategories().size());
        assertEquals(3L, result.getCategories().getFirst().getId());

        assertEquals(1, result.getVariations().size());
        ProductVariationGetVm variationVm = result.getVariations().getFirst();
        assertEquals(200L, variationVm.id());
        assertEquals("variant-1", variationVm.slug());
        assertEquals("thumb-url", variationVm.thumbnail().url());
        assertEquals(1, variationVm.productImages().size());
        assertEquals("image-url", variationVm.productImages().getFirst().url());
        assertEquals("Red", variationVm.options().get(7L));
    }
}
