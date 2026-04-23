package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.ProductOptionValue;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.product.ProductExportingDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductSlugGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionValueRepository productOptionValueRepository;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getLatestProducts_whenCountNonPositive_returnsEmpty() {
        assertTrue(productService.getLatestProducts(0).isEmpty());
        verifyNoInteractions(productRepository);
    }

    @Test
    void getLatestProducts_whenRepositoryEmpty_returnsEmpty() {
        when(productRepository.getLatestProducts(PageRequest.of(0, 3))).thenReturn(List.of());

        assertTrue(productService.getLatestProducts(3).isEmpty());
        verify(productRepository).getLatestProducts(PageRequest.of(0, 3));
    }

    @Test
    void getLatestProducts_whenRepositoryReturnsProducts_mapsToListVm() {
        Product product = new Product();
        product.setId(88L);
        product.setName("Product88");
        product.setSlug("product-88");
        product.setPrice(15.0);
        product.setPublished(true);
        product.setAllowedToOrder(true);
        product.setFeatured(false);
        product.setVisibleIndividually(true);
        product.setTaxClassId(2L);

        when(productRepository.getLatestProducts(PageRequest.of(0, 1))).thenReturn(List.of(product));

        var result = productService.getLatestProducts(1);

        assertEquals(1, result.size());
        assertEquals(88L, result.getFirst().id());
        assertEquals("product-88", result.getFirst().slug());
        assertEquals(15.0, result.getFirst().price());
    }

    @Test
    void getProductsByBrand_whenBrandMissing_throwsNotFoundException() {
        when(brandRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("unknown"));
    }

    @Test
    void getProductsByBrand_returnsThumbnailVms() {
        Brand brand = new Brand();
        brand.setId(7L);
        brand.setName("BrandA");
        brand.setSlug("brand-a");

        Product product = new Product();
        product.setId(101L);
        product.setName("ProductA");
        product.setSlug("product-a");
        product.setThumbnailMediaId(10L);

        when(brandRepository.findBySlug("brand-a")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(10L)).thenReturn(new NoFileMediaVm(10L, "", "", "", "thumb-url"));

        List<ProductThumbnailVm> result = productService.getProductsByBrand("brand-a");

        assertEquals(1, result.size());
        assertEquals(101L, result.getFirst().id());
        assertEquals("thumb-url", result.getFirst().thumbnailUrl());
    }

    @Test
    void createProduct_withoutVariations_returnsBaseDetail() {
        Category category = new Category();
        category.setId(2L);

        ProductPostVm postVm = new ProductPostVm(
                "Product-A",
                "product-a",
                null,
                List.of(2L),
                "short",
                "desc",
                "spec",
                "sku-a",
                "gtin-a",
                1.0,
                DimensionUnit.CM,
                2.0,
                1.0,
                1.0,
                10.0,
                true,
                true,
                false,
                true,
                false,
                "meta",
                "kw",
                "md",
                11L,
                List.of(11L),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                1L);

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Product-A");
        saved.setSlug("product-a");

        when(productRepository.findBySlugAndIsPublishedTrue("product-a")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("gtin-a")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("sku-a")).thenReturn(Optional.empty());
        when(categoryRepository.findAllById(List.of(2L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductGetDetailVm result = productService.createProduct(postVm);

        assertEquals(1L, result.id());
        assertEquals("Product-A", result.name());
        assertEquals("product-a", result.slug());
    }

    @Test
    void createProduct_withVariationsAndOptions_createsCombinations() {
        Brand brand = new Brand();
        brand.setId(15L);

        Category category = new Category();
        category.setId(5L);

        ProductPostVm postVm = new ProductPostVm(
                "Product-B",
                "product-b",
                15L,
                List.of(5L),
                "short",
                "desc",
                "spec",
                "sku-b",
                "gtin-b",
                1.0,
                DimensionUnit.CM,
                3.0,
                2.0,
                1.0,
                20.0,
                true,
                true,
                false,
                true,
                false,
                "meta",
                "kw",
                "md",
                21L,
                List.of(21L),
                List.of(new ProductVariationPostVm(
                        "Variant-1",
                        "variant-1",
                        "sku-v1",
                        "gtin-v1",
                        22.0,
                        31L,
                        List.of(31L),
                        Map.of(55L, "Red"))),
                List.of(new ProductOptionValuePostVm(55L, "text", 1, List.of("Red"))),
                List.of(ProductOptionValueDisplay.builder()
                        .productOptionId(55L)
                        .displayType("text")
                        .displayOrder(1)
                        .value("Red")
                        .build()),
                List.of(900L),
                2L);

        Product savedMain = new Product();
        savedMain.setId(2L);
        savedMain.setName("Product-B");
        savedMain.setSlug("product-b");
        savedMain.setPublished(true);

        Product savedVariation = new Product();
        savedVariation.setId(3L);
        savedVariation.setSlug("variant-1");
        savedVariation.setPublished(true);

        Product related = new Product();
        related.setId(900L);

        ProductOption option = new ProductOption();
        option.setId(55L);

        ProductOptionValue optionValue = ProductOptionValue.builder()
                .product(savedMain)
                .productOption(option)
                .displayOrder(1)
                .displayType("text")
                .value("Red")
                .build();

        when(productRepository.findBySlugAndIsPublishedTrue("product-b")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("gtin-b")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("sku-b")).thenReturn(Optional.empty());
        when(categoryRepository.findAllById(List.of(5L))).thenReturn(List.of(category));
        when(brandRepository.findById(15L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(savedMain);
        when(productRepository.saveAll(any())).thenReturn(List.of(savedVariation));
        when(productRepository.findAllById(eq(List.of(900L)))).thenReturn(List.of(related));
        when(productRepository.findAllById(eq(List.of()))).thenReturn(List.of());
        when(productOptionRepository.findAllByIdIn(List.of(55L))).thenReturn(List.of(option));
        when(productOptionValueRepository.saveAll(any())).thenReturn(List.of(optionValue));

        ProductGetDetailVm result = productService.createProduct(postVm);

        assertEquals(2L, result.id());
        assertEquals("product-b", result.slug());
        verify(productOptionCombinationRepository).saveAll(any());
        verify(productRelatedRepository).saveAll(any());
    }

    @Test
    void getProductById_whenImagesAndBrandPresent_mapsDetailVm() {
        Product product = new Product();
        product.setId(33L);
        product.setName("Product-33");
        product.setSlug("product-33");
        product.setShortDescription("short");
        product.setDescription("desc");
        product.setSpecification("spec");
        product.setSku("sku-33");
        product.setGtin("gtin-33");
        product.setAllowedToOrder(true);
        product.setPublished(true);
        product.setFeatured(true);
        product.setVisibleIndividually(true);
        product.setStockTrackingEnabled(false);
        product.setPrice(42.0);
        product.setThumbnailMediaId(91L);
        product.setTaxClassId(3L);

        Product parent = new Product();
        parent.setId(44L);
        product.setParent(parent);

        Brand brand = new Brand();
        brand.setId(7L);
        product.setBrand(brand);

        Category category = new Category();
        category.setId(5L);
        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
        product.setProductCategories(List.of(productCategory));

        ProductImage productImage = ProductImage.builder()
                .imageId(101L)
                .product(product)
                .build();
        product.setProductImages(List.of(productImage));

        when(productRepository.findById(33L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(91L)).thenReturn(new NoFileMediaVm(91L, "", "", "", "thumb-url"));
        when(mediaService.getMedia(101L)).thenReturn(new NoFileMediaVm(101L, "", "", "", "image-url"));

        ProductDetailVm result = productService.getProductById(33L);

        assertEquals(33L, result.id());
        assertEquals(7L, result.brandId());
        assertEquals(44L, result.parentId());
        assertEquals("thumb-url", result.thumbnailMedia().url());
        assertEquals(1, result.productImageMedias().size());
        assertEquals("image-url", result.productImageMedias().getFirst().url());
    }

    @Test
    void getProductById_whenNoImagesAndBrandNull_handlesNulls() {
        Product product = new Product();
        product.setId(34L);
        product.setName("Product-34");
        product.setSlug("product-34");
        product.setProductImages(null);
        product.setProductCategories(null);
        product.setThumbnailMediaId(null);
        product.setBrand(null);

        when(productRepository.findById(34L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(34L);

        assertEquals(34L, result.id());
        assertEquals(null, result.brandId());
        assertEquals(null, result.thumbnailMedia());
        assertEquals(0, result.productImageMedias().size());
        assertEquals(0, result.categories().size());
    }

    @Test
    void getProductSlug_whenParentPresent_returnsParentSlug() {
        Product parent = new Product();
        parent.setId(1L);
        parent.setSlug("parent-slug");

        Product child = new Product();
        child.setId(11L);
        child.setParent(parent);
        child.setSlug("child-slug");

        when(productRepository.findById(11L)).thenReturn(Optional.of(child));

        ProductSlugGetVm result = productService.getProductSlug(11L);

        assertEquals("parent-slug", result.slug());
        assertEquals(11L, result.productVariantId());
    }

    @Test
    void getProductSlug_whenNoParent_returnsOwnSlug() {
        Product product = new Product();
        product.setId(12L);
        product.setSlug("own-slug");

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(12L);

        assertEquals("own-slug", result.slug());
        assertEquals(null, result.productVariantId());
    }

    @Test
    void getProductSlug_whenMissing_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductSlug(99L));
    }

    @Test
    void getFeaturedProductsById_whenThumbnailEmpty_usesParentThumbnail() {
        Product parent = new Product();
        parent.setId(70L);
        parent.setThumbnailMediaId(300L);

        Product child = new Product();
        child.setId(71L);
        child.setName("Child");
        child.setSlug("child");
        child.setPrice(20.0);
        child.setParent(parent);
        child.setThumbnailMediaId(200L);

        when(productRepository.findAllByIdIn(List.of(71L))).thenReturn(List.of(child));
        when(mediaService.getMedia(200L)).thenReturn(new NoFileMediaVm(200L, "", "", "", ""));
        when(productRepository.findById(70L)).thenReturn(Optional.of(parent));
        when(mediaService.getMedia(300L)).thenReturn(new NoFileMediaVm(300L, "", "", "", "parent-url"));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(71L));

        assertEquals(1, result.size());
        assertEquals("parent-url", result.getFirst().thumbnailUrl());
    }

    @Test
    void getFeaturedProductsById_whenThumbnailPresent_usesOwnThumbnail() {
        Product product = new Product();
        product.setId(80L);
        product.setName("Main");
        product.setSlug("main");
        product.setPrice(30.0);
        product.setThumbnailMediaId(400L);

        when(productRepository.findAllByIdIn(List.of(80L))).thenReturn(List.of(product));
        when(mediaService.getMedia(400L)).thenReturn(new NoFileMediaVm(400L, "", "", "", "own-url"));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(80L));

        assertEquals(1, result.size());
        assertEquals("own-url", result.getFirst().thumbnailUrl());
    }

    @Test
    void getProductVariationsByParentId_whenHasOptions_returnsPublishedVariations() {
        Product parent = new Product();
        parent.setId(5L);
        parent.setHasOptions(true);

        Product publishedVariation = new Product();
        publishedVariation.setId(6L);
        publishedVariation.setName("variant-1");
        publishedVariation.setSlug("variant-1");
        publishedVariation.setSku("sku-1");
        publishedVariation.setGtin("gtin-1");
        publishedVariation.setPrice(12.5);
        publishedVariation.setPublished(true);
        publishedVariation.setThumbnailMediaId(100L);

        ProductImage variationImage = ProductImage.builder()
                .imageId(200L)
                .product(publishedVariation)
                .build();
        publishedVariation.setProductImages(List.of(variationImage));

        Product hiddenVariation = new Product();
        hiddenVariation.setId(7L);
        hiddenVariation.setPublished(false);

        parent.setProducts(List.of(publishedVariation, hiddenVariation));

        ProductOption option = new ProductOption();
        option.setId(9L);

        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(publishedVariation);
        combination.setProductOption(option);
        combination.setValue("Red");

        when(productRepository.findById(5L)).thenReturn(Optional.of(parent));
        when(productOptionCombinationRepository.findAllByProduct(publishedVariation))
                .thenReturn(List.of(combination));
        when(mediaService.getMedia(100L)).thenReturn(new NoFileMediaVm(100L, "", "", "", "thumb-url"));
        when(mediaService.getMedia(200L)).thenReturn(new NoFileMediaVm(200L, "", "", "", "image-url"));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(5L);

        assertEquals(1, result.size());
        ProductVariationGetVm variationVm = result.getFirst();
        assertEquals(6L, variationVm.id());
        assertEquals("variant-1", variationVm.slug());
        assertNotNull(variationVm.thumbnail());
        assertEquals("thumb-url", variationVm.thumbnail().url());
        assertEquals(1, variationVm.productImages().size());
        assertEquals("image-url", variationVm.productImages().getFirst().url());
        assertEquals("Red", variationVm.options().get(9L));
    }

    @Test
    void getProductVariationsByParentId_whenNoOptions_returnsEmpty() {
        Product parent = new Product();
        parent.setId(9L);
        parent.setHasOptions(false);

        when(productRepository.findById(9L)).thenReturn(Optional.of(parent));

        assertTrue(productService.getProductVariationsByParentId(9L).isEmpty());
    }

    @Test
    void getProductVariationsByParentId_whenParentMissing_throwsNotFoundException() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductVariationsByParentId(404L));
    }

    @Test
    void getProductDetail_whenImagesAndCategoriesPresent_mapsDetailGetVm() {
        Product product = new Product();
        product.setId(90L);
        product.setName("Product-90");
        product.setShortDescription("short-90");
        product.setDescription("desc-90");
        product.setSpecification("spec-90");
        product.setAllowedToOrder(true);
        product.setPublished(true);
        product.setFeatured(false);
        product.setHasOptions(false);
        product.setPrice(11.5);
        product.setThumbnailMediaId(500L);
        product.setAttributeValues(List.of());

        Category category = new Category();
        category.setId(10L);
        category.setName("CatA");
        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
        product.setProductCategories(List.of(productCategory));

        ProductImage productImage = ProductImage.builder()
                .imageId(600L)
                .product(product)
                .build();
        product.setProductImages(List.of(productImage));

        when(productRepository.findBySlugAndIsPublishedTrue("product-90")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(500L)).thenReturn(new NoFileMediaVm(500L, "", "", "", "thumb-90"));
        when(mediaService.getMedia(600L)).thenReturn(new NoFileMediaVm(600L, "", "", "", "image-90"));

        ProductDetailGetVm result = productService.getProductDetail("product-90");

        assertEquals(90L, result.id());
        assertEquals("thumb-90", result.thumbnailMediaUrl());
        assertEquals(1, result.productCategories().size());
        assertEquals("CatA", result.productCategories().getFirst());
        assertEquals(1, result.productImageMediaUrls().size());
        assertEquals("image-90", result.productImageMediaUrls().getFirst());
    }

    @Test
    void getProductDetail_whenAttributesGrouped_includesNoneGroup() {
        Product product = new Product();
        product.setId(95L);
        product.setName("Product-95");
        product.setShortDescription("short-95");
        product.setDescription("desc-95");
        product.setSpecification("spec-95");
        product.setAllowedToOrder(true);
        product.setPublished(true);
        product.setFeatured(false);
        product.setHasOptions(false);
        product.setPrice(12.5);
        product.setThumbnailMediaId(550L);

        Category category = new Category();
        category.setId(11L);
        category.setName("CatB");
        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
        product.setProductCategories(List.of(productCategory));

        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setName("GroupA");
        ProductAttribute groupedAttribute = ProductAttribute.builder()
                .name("Material")
                .productAttributeGroup(group)
                .build();
        ProductAttribute ungroupedAttribute = ProductAttribute.builder()
                .name("Origin")
                .productAttributeGroup(null)
                .build();

        ProductAttributeValue groupedValue = new ProductAttributeValue();
        groupedValue.setProduct(product);
        groupedValue.setProductAttribute(groupedAttribute);
        groupedValue.setValue("Steel");

        ProductAttributeValue ungroupedValue = new ProductAttributeValue();
        ungroupedValue.setProduct(product);
        ungroupedValue.setProductAttribute(ungroupedAttribute);
        ungroupedValue.setValue("VN");

        product.setAttributeValues(List.of(groupedValue, ungroupedValue));

        when(productRepository.findBySlugAndIsPublishedTrue("product-95")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(550L)).thenReturn(new NoFileMediaVm(550L, "", "", "", "thumb-95"));

        ProductDetailGetVm result = productService.getProductDetail("product-95");

        assertEquals(2, result.productAttributeGroups().size());
        var groupNames = result.productAttributeGroups().stream().map(groupVm -> groupVm.name()).toList();
        assertTrue(groupNames.contains("GroupA"));
        assertTrue(groupNames.contains("None group"));
    }

    @Test
    void getProductsWithFilter_mapsPagedResult() {
        Product product = new Product();
        product.setId(501L);
        product.setName("FilterProduct");
        product.setSlug("filter-product");
        product.setPrice(8.5);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(1, 2), 3);

        when(productRepository.getProductsWithFilter(eq("item"), eq("brand"), eq(PageRequest.of(1, 2))))
                .thenReturn(page);

        ProductListGetVm result = productService.getProductsWithFilter(1, 2, " Item ", "brand");

        assertEquals(1, result.productContent().size());
        assertEquals(1, result.pageNo());
        assertEquals(2, result.pageSize());
        assertEquals(3, result.totalElements());
    }

    @Test
    void getProductsFromCategory_mapsPagedResult() {
        Category category = new Category();
        category.setId(3000L);
        category.setSlug("cat-slug");

        Product product = new Product();
        product.setId(3001L);
        product.setName("CategoryProduct");
        product.setSlug("category-product");
        product.setThumbnailMediaId(3100L);

        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();

        Page<ProductCategory> page = new PageImpl<>(List.of(productCategory), PageRequest.of(0, 1), 1);

        when(categoryRepository.findBySlug("cat-slug")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(PageRequest.of(0, 1), category)).thenReturn(page);
        when(mediaService.getMedia(3100L)).thenReturn(new NoFileMediaVm(3100L, "", "", "", "cat-url"));

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 1, "cat-slug");

        assertEquals(1, result.productContent().size());
        assertEquals("cat-url", result.productContent().getFirst().thumbnailUrl());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getProductEsDetailById_mapsCategoryAndAttributeNames() {
        Product product = new Product();
        product.setId(601L);
        product.setName("EsProduct");
        product.setSlug("es-product");
        product.setPrice(19.0);
        product.setPublished(true);
        product.setVisibleIndividually(true);
        product.setAllowedToOrder(true);
        product.setFeatured(false);
        product.setThumbnailMediaId(700L);

        Brand brand = new Brand();
        brand.setName("BrandX");
        product.setBrand(brand);

        Category category = new Category();
        category.setName("CatX");
        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
        product.setProductCategories(List.of(productCategory));

        ProductAttribute attribute = ProductAttribute.builder().name("Size").build();
        ProductAttributeValue attributeValue = new ProductAttributeValue();
        attributeValue.setProductAttribute(attribute);
        attributeValue.setProduct(product);
        attributeValue.setValue("L");
        product.setAttributeValues(List.of(attributeValue));

        when(productRepository.findById(601L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(601L);

        assertEquals("BrandX", result.brand());
        assertEquals(List.of("CatX"), result.categories());
        assertEquals(List.of("Size"), result.attributes());
        assertEquals(700L, result.thumbnailMediaId());
    }

    @Test
    void exportProducts_mapsBrandFields() {
        Brand brand = new Brand();
        brand.setId(4000L);
        brand.setName("BrandExport");

        Product product = new Product();
        product.setId(4001L);
        product.setName("ExportMe");
        product.setShortDescription("short");
        product.setDescription("desc");
        product.setSpecification("spec");
        product.setSku("sku");
        product.setGtin("gtin");
        product.setSlug("export");
        product.setAllowedToOrder(true);
        product.setPublished(true);
        product.setFeatured(false);
        product.setVisibleIndividually(true);
        product.setStockTrackingEnabled(true);
        product.setPrice(33.3);
        product.setBrand(brand);
        product.setMetaTitle("meta");
        product.setMetaKeyword("kw");
        product.setMetaDescription("md");

        when(productRepository.getExportingProducts(eq("name"), eq("brand"))).thenReturn(List.of(product));

        List<ProductExportingDetailVm> result = productService.exportProducts("Name", "brand");

        assertEquals(1, result.size());
        assertEquals(4000L, result.getFirst().brandId());
        assertEquals("BrandExport", result.getFirst().brandName());
    }

    @Test
    void deleteProduct_whenParentPresent_deletesCombinationsAndUnpublishes() {
        Product parent = new Product();
        parent.setId(800L);

        Product product = new Product();
        product.setId(801L);
        product.setParent(parent);
        product.setPublished(true);

        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(product);

        when(productRepository.findById(801L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product))
                .thenReturn(List.of(combination));

        productService.deleteProduct(801L);

        assertFalse(product.isPublished());
        verify(productOptionCombinationRepository).deleteAll(List.of(combination));
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenNoParent_skipsCombinationDelete() {
        Product product = new Product();
        product.setId(900L);
        product.setPublished(true);

        when(productRepository.findById(900L)).thenReturn(Optional.of(product));

        productService.deleteProduct(900L);

        assertFalse(product.isPublished());
        verify(productRepository).save(product);
        verifyNoInteractions(productOptionCombinationRepository);
    }

    @Test
    void getProductCheckoutList_whenThumbnailEmpty_keepsDefault() {
        Brand brand = new Brand();
        brand.setId(901L);

        Product product = new Product();
        product.setId(902L);
        product.setName("Checkout");
        product.setBrand(brand);
        product.setPrice(12.0);
        product.setThumbnailMediaId(1000L);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 1), 1);

        when(productRepository.findAllPublishedProductsByIds(eq(List.of(902L)), eq(PageRequest.of(0, 1))))
                .thenReturn(page);
        when(mediaService.getMedia(1000L)).thenReturn(new NoFileMediaVm(1000L, "", "", "", ""));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 1, List.of(902L));

        assertEquals(1, result.productCheckoutListVms().size());
        assertEquals("", result.productCheckoutListVms().getFirst().thumbnailUrl());
        verifyNoMoreInteractions(productOptionCombinationRepository);
    }

    @Test
    void getProductCheckoutList_whenThumbnailPresent_setsThumbnail() {
        Brand brand = new Brand();
        brand.setId(910L);

        Product product = new Product();
        product.setId(911L);
        product.setName("Checkout2");
        product.setBrand(brand);
        product.setPrice(14.0);
        product.setThumbnailMediaId(1010L);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(1, 1), 1);

        when(productRepository.findAllPublishedProductsByIds(eq(List.of(911L)), eq(PageRequest.of(1, 1))))
                .thenReturn(page);
        when(mediaService.getMedia(1010L)).thenReturn(new NoFileMediaVm(1010L, "", "", "", "thumb-1010"));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(1, 1, List.of(911L));

        assertEquals(1, result.productCheckoutListVms().size());
        assertEquals("thumb-1010", result.productCheckoutListVms().getFirst().thumbnailUrl());
    }

    @Test
    void getProductsByMultiQuery_mapsThumbnailUrl() {
        Product product = new Product();
        product.setId(1001L);
        product.setName("MultiQuery");
        product.setSlug("multi-query");
        product.setPrice(9.0);
        product.setThumbnailMediaId(1100L);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 2), 1);

        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
                eq("query"), eq("cat"), eq(1.0), eq(10.0), eq(PageRequest.of(0, 2))))
                .thenReturn(page);
        when(mediaService.getMedia(1100L)).thenReturn(new NoFileMediaVm(1100L, "", "", "", "mq-url"));

        var result = productService.getProductsByMultiQuery(0, 2, "Query", "cat", 1.0, 10.0);

        assertEquals(1, result.productContent().size());
        assertEquals("mq-url", result.productContent().getFirst().thumbnailUrl());
    }

    @Test
    void getRelatedProductsBackoffice_returnsRelatedList() {
        Product related = new Product();
        related.setId(1201L);
        related.setName("Related");
        related.setSlug("related");
        related.setAllowedToOrder(true);
        related.setPublished(true);
        related.setFeatured(false);
        related.setVisibleIndividually(true);
        related.setPrice(22.0);
        related.setTaxClassId(1L);

        Product product = new Product();
        product.setId(1200L);

        ProductRelated relation = ProductRelated.builder()
                .product(product)
                .relatedProduct(related)
                .build();
        product.setRelatedProducts(List.of(relation));

        when(productRepository.findById(1200L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1200L);

        assertEquals(1, result.size());
        assertEquals(1201L, result.getFirst().id());
        assertEquals("related", result.getFirst().slug());
    }

    @Test
    void getRelatedProductsStorefront_filtersUnpublished() {
        Product relatedPublished = new Product();
        relatedPublished.setId(1301L);
        relatedPublished.setName("Published");
        relatedPublished.setSlug("published");
        relatedPublished.setPrice(15.0);
        relatedPublished.setPublished(true);
        relatedPublished.setThumbnailMediaId(1400L);

        Product relatedHidden = new Product();
        relatedHidden.setId(1302L);
        relatedHidden.setPublished(false);

        Product product = new Product();
        product.setId(1300L);

        ProductRelated relationPublished = ProductRelated.builder()
                .product(product)
                .relatedProduct(relatedPublished)
                .build();
        ProductRelated relationHidden = ProductRelated.builder()
                .product(product)
                .relatedProduct(relatedHidden)
                .build();

        Page<ProductRelated> page = new PageImpl<>(List.of(relationPublished, relationHidden),
                PageRequest.of(0, 2), 2);

        when(productRepository.findById(1300L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(product, PageRequest.of(0, 2))).thenReturn(page);
        when(mediaService.getMedia(1400L)).thenReturn(new NoFileMediaVm(1400L, "", "", "", "rel-url"));

        var result = productService.getRelatedProductsStorefront(1300L, 0, 2);

        assertEquals(1, result.productContent().size());
        assertEquals("rel-url", result.productContent().getFirst().thumbnailUrl());
    }

    @Test
    void updateProductQuantity_updatesStockQuantity() {
        Product product = new Product();
        product.setId(1500L);
        product.setStockQuantity(5L);

        when(productRepository.findAllByIdIn(List.of(1500L))).thenReturn(List.of(product));

        productService.updateProductQuantity(List.of(new ProductQuantityPostVm(1500L, 9L)));

        assertEquals(9L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void subtractStockQuantity_appliesFloorAtZero() {
        Product product = new Product();
        product.setId(1600L);
        product.setStockQuantity(3L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(List.of(1600L))).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1600L, 5L)));

        assertEquals(0L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void restoreStockQuantity_addsQuantity() {
        Product product = new Product();
        product.setId(1700L);
        product.setStockQuantity(4L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(List.of(1700L))).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(new ProductQuantityPutVm(1700L, 6L)));

        assertEquals(10L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void getProductByIds_mapsListVm() {
        Product product = new Product();
        product.setId(1800L);
        product.setName("ById");
        product.setSlug("by-id");

        when(productRepository.findAllByIdIn(List.of(1800L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByIds(List.of(1800L));

        assertEquals(1, result.size());
        assertEquals("by-id", result.getFirst().slug());
    }

    @Test
    void getProductByCategoryIds_mapsListVm() {
        Product product = new Product();
        product.setId(1900L);
        product.setName("ByCat");
        product.setSlug("by-cat");

        when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByCategoryIds(List.of(1L));

        assertEquals(1, result.size());
        assertEquals(1900L, result.getFirst().id());
    }

    @Test
    void getProductByBrandIds_mapsListVm() {
        Product product = new Product();
        product.setId(2000L);
        product.setName("ByBrand");
        product.setSlug("by-brand");

        when(productRepository.findByBrandIdsIn(List.of(2L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByBrandIds(List.of(2L));

        assertEquals(1, result.size());
        assertEquals("by-brand", result.getFirst().slug());
    }

    @Test
    void getProductsForWarehouse_mapsListVm() {
        Product product = new Product();
        product.setId(2100L);
        product.setName("Warehouse");
        product.setSlug("warehouse");

        when(productRepository.findProductForWarehouse(eq("name"), eq("sku"), eq(List.of(2100L)), eq("ALL")))
                .thenReturn(List.of(product));

        var result = productService.getProductsForWarehouse("name", "sku", List.of(2100L),
                FilterExistInWhSelection.ALL);

        assertEquals(1, result.size());
        assertEquals(2100L, result.getFirst().id());
    }

    @Test
    void getListFeaturedProducts_mapsPagedResult() {
        Product product = new Product();
        product.setId(2200L);
        product.setName("Featured");
        product.setSlug("featured");
        product.setPrice(19.9);
        product.setThumbnailMediaId(2300L);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 1), 1);

        when(productRepository.getFeaturedProduct(PageRequest.of(0, 1))).thenReturn(page);
        when(mediaService.getMedia(2300L)).thenReturn(new NoFileMediaVm(2300L, "", "", "", "feat-url"));

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 1);

        assertEquals(1, result.productList().size());
        assertEquals("feat-url", result.productList().getFirst().thumbnailUrl());
        assertEquals(1, result.totalPage());
    }
}
