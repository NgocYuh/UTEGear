package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Brand;
import com.utegear.entity.Category;
import com.utegear.exception.ApiException;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogReferenceServiceTest {

    @Test
    void createsNormalizedBrand() {
        BrandRepository repository = mock(BrandRepository.class);
        when(repository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new BrandService(repository).create("  Logitech  ", "Gear");

        assertThat(result.name()).isEqualTo("Logitech");
        assertThat(result.slug()).isEqualTo("logitech");
    }

    @Test
    void rejectsDuplicateBrand() {
        BrandRepository repository = mock(BrandRepository.class);
        when(repository.existsByNameIgnoreCase("Logitech")).thenReturn(true);

        assertThatThrownBy(() -> new BrandService(repository).create(" Logitech ", null))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("BRAND_ALREADY_EXISTS");
    }

    @Test
    void returnsActiveCategoriesInRepositoryOrder() {
        CategoryRepository repository = mock(CategoryRepository.class);
        when(repository.findByActiveTrueOrderBySortOrderAscNameAsc()).thenReturn(List.of(
                new Category("Keyboard", "keyboard", null, 1),
                new Category("Mouse", "mouse", null, 2)));

        var result = new CategoryService(repository).active();

        assertThat(result).extracting("slug").containsExactly("keyboard", "mouse");
        verify(repository).findByActiveTrueOrderBySortOrderAscNameAsc();
    }

    @Test
    void rejectsDuplicateCategorySlug() {
        CategoryRepository repository = mock(CategoryRepository.class);
        when(repository.existsBySlug("ban-phim-co")).thenReturn(true);

        assertThatThrownBy(() -> new CategoryService(repository).create("Bàn phím cơ", null, 1))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("CATEGORY_ALREADY_EXISTS");
    }
}
