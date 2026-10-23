package com.utegear.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.catalog.CategorySummary;
import com.utegear.dto.catalog.ProductDtos;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.StoreService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

@ExtendWith(MockitoExtension.class)
class StorefrontViewControllerTest {

    @Mock private CategoryService categoryService;
    @Mock private BrandService brandService;
    @Mock private ProductCatalogService productCatalogService;
    @Mock private StoreService storeService;

    private StorefrontViewController controller;
    private ProductDtos.ProductPage emptyProducts;

    @BeforeEach
    void setUp() {
        controller = new StorefrontViewController(
                categoryService, brandService, productCatalogService, storeService);
        emptyProducts = new ProductDtos.ProductPage(List.of(), 0, 20, 0, 0, true, true, true);
    }

    @Test
    void homeUsesCatalogServicesAndReturnsHomeTemplate() {
        when(productCatalogService.search(null, null, null, null, null, 0, 8, "createdAt,desc"))
                .thenReturn(emptyProducts);
        when(categoryService.active()).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.home(model)).isEqualTo("home");
        assertThat(model.getAttribute("categories")).isEqualTo(List.of());
        assertThat(model.getAttribute("featuredProducts")).isEqualTo(List.of());
    }

    @Test
    void productsExposesContractPageAndFilterModels() {
        when(productCatalogService.search("mouse", null, null, null, null, 0, 20, "createdAt,desc"))
                .thenReturn(emptyProducts);
        when(categoryService.active()).thenReturn(List.of());
        when(brandService.active()).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.products(
                "mouse", null, null, null, null, 0, 20, "createdAt,desc", model))
                .isEqualTo("products");
        assertThat(model.getAttribute("products")).isSameAs(emptyProducts);
        assertThat(model.getAttribute("keyword")).isEqualTo("mouse");
        assertThat(model.getAttribute("allBrands")).isEqualTo(List.of());
    }

    @Test
    void categoryAliasesResolveLegacyNavigationSlugs() {
        CategorySummary keyboard = new CategorySummary(1L, "Bàn phím", "ban-phim", 0);
        when(categoryService.activeBySlug("ban-phim")).thenReturn(keyboard);
        when(productCatalogService.search(
                null, 1L, null, null, null, null, 0, 20, "createdAt,desc"))
                .thenReturn(emptyProducts);
        when(categoryService.active()).thenReturn(List.of(keyboard));
        when(brandService.active()).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.category(
                "keyboard", null, null, null, null, 0, 20, "createdAt,desc", model))
                .isEqualTo("products");
        assertThat(model.getAttribute("selectedCategoryId")).isEqualTo(1L);
        assertThat(model.getAttribute("currentCategory")).isEqualTo(keyboard);
        assertThat(model.getAttribute("categoryBaseUrl")).isEqualTo("/category/ban-phim");
        verify(categoryService).activeBySlug("ban-phim");
    }

    @Test
    void customerShellsStartWithSafeEmptyModels() {
        ConcurrentModel cart = new ConcurrentModel();
        ConcurrentModel wishlist = new ConcurrentModel();

        assertThat(controller.cart(cart)).isEqualTo("cart");
        assertThat(cart.getAttribute("cartItems")).isEqualTo(List.of());
        assertThat(controller.wishlist(wishlist)).isEqualTo("wishlist");
        assertThat(wishlist.getAttribute("wishlistItems")).isEqualTo(List.of());
    }

    @Test
    void staticRoutesResolveExistingTemplates() {
        assertThat(controller.login()).isEqualTo("auth/login");
        assertThat(controller.register()).isEqualTo("auth/register");
        assertThat(controller.verifyOtp()).isEqualTo("auth/verify-otp");
        assertThat(controller.forgotPassword()).isEqualTo("auth/forgot-password");
        assertThat(controller.about()).isEqualTo("about");
        assertThat(controller.contact()).isEqualTo("contact");
        assertThat(controller.profile()).isEqualTo("profile/profile");
    }

    @Test
    void showroomLoadsProductsFromTheRequestedStoresInventory() {
        var product = org.mockito.Mockito.mock(ProductDtos.Summary.class);
        when(productCatalogService.inStockAtStore(2L)).thenReturn(List.of(product));
        ConcurrentModel model = new ConcurrentModel();
        assertThat(controller.store(2L, model)).isEqualTo("stores/store-detail");
        assertThat(model.getAttribute("products")).isEqualTo(List.of(product));
        verify(storeService).findPublicDetail(2L);
        verify(productCatalogService).inStockAtStore(2L);
    }
}
