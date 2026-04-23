package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.product.ProductApplication;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.category.CategoryGetDetailVm;
import com.yas.product.viewmodel.category.CategoryGetVm;
import com.yas.product.viewmodel.category.CategoryPostVm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ProductApplication.class)
class CategoryServiceTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @MockitoBean
    private MediaService mediaService;
    @Autowired
    private CategoryService categoryService;

    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {

        category = new Category();
        category.setName("name");
        category.setSlug("slug");
        category.setDescription("description");
        category.setMetaKeyword("metaKeyword");
        category.setMetaDescription("metaDescription");
        category.setDisplayOrder((short) 1);
        category.setIsPublished(true);
        category.setImageId(1L);
        categoryRepository.save(category);

        noFileMediaVm = new NoFileMediaVm(1L, "caption", "fileName", "mediaType", "url");
    }

    @AfterEach
    void tearDown() {
        productCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void getCategoryById_Success() {
        when(mediaService.getMedia(category.getImageId())).thenReturn(noFileMediaVm);
        CategoryGetDetailVm categoryGetDetailVm = categoryService.getCategoryById(category.getId());
        assertNotNull(categoryGetDetailVm);
        assertEquals("name", categoryGetDetailVm.name());
    }

    @Test
    void getCategories_Success() {
        when(mediaService.getMedia(any())).thenReturn(noFileMediaVm);
        Assertions.assertEquals(1, categoryService.getCategories("name").size());
        CategoryGetVm categoryGetVm = categoryService.getCategories("name").getFirst();
        assertEquals("name", categoryGetVm.name());
    }

    @Test
    void getCategoriesPageable_Success() {
        when(mediaService.getMedia(category.getImageId())).thenReturn(noFileMediaVm);
        Assertions.assertEquals(1, categoryService.getPageableCategories(0, 1).categoryContent().size());
        CategoryGetVm categoryGetVm = categoryService.getCategories("a").getFirst();
        assertEquals("name", categoryGetVm.name());
    }

    @Test
    void create_WithParent_Success() {
        Category parent = new Category();
        parent.setName("parent");
        parent.setSlug("parent");
        parent.setIsPublished(true);
        categoryRepository.save(parent);

        CategoryPostVm vm = new CategoryPostVm(
                "child",
                "child",
                "desc",
                parent.getId(),
                "meta",
                "metaDesc",
                (short) 1,
                true,
                null);

        Category created = categoryService.create(vm);
        assertNotNull(created.getParent());
        assertEquals(parent.getId(), created.getParent().getId());
    }

    @Test
    void create_WithDuplicateName_Throws() {
        CategoryPostVm vm = new CategoryPostVm(
                "name",
                "slug-2",
                "desc",
                null,
                "meta",
                "metaDesc",
                (short) 1,
                true,
                null);

        assertThrows(DuplicatedException.class, () -> categoryService.create(vm));
    }

    @Test
    void create_WithMissingParent_Throws() {
        CategoryPostVm vm = new CategoryPostVm(
                "child",
                "child",
                "desc",
                999L,
                "meta",
                "metaDesc",
                (short) 1,
                true,
                null);

        assertThrows(BadRequestException.class, () -> categoryService.create(vm));
    }

    @Test
    void update_ParentNull_ClearsParent() {
        Category parent = new Category();
        parent.setName("parent");
        parent.setSlug("parent");
        parent.setIsPublished(true);
        categoryRepository.save(parent);

        category.setParent(parent);
        categoryRepository.save(category);

        CategoryPostVm vm = new CategoryPostVm(
                "name",
                "slug",
                "desc",
                null,
                "meta",
                "metaDesc",
                (short) 1,
                true,
                null);

        categoryService.update(vm, category.getId());
        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertNull(updated.getParent());
    }

    @Test
    void update_ParentIsSelf_Throws() {
        CategoryPostVm vm = new CategoryPostVm(
                "name",
                "slug",
                "desc",
                category.getId(),
                "meta",
                "metaDesc",
                (short) 1,
                true,
                null);

        assertThrows(BadRequestException.class, () -> categoryService.update(vm, category.getId()));
    }
}