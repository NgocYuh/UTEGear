package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import com.utegear.entity.WishlistItem;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.WishlistItemRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WishlistServiceTest {

    private WishlistItemRepository wishlistRepository;
    private ProductRepository productRepository;
    private StoreInventoryRepository inventoryRepository;
    private WishlistService service;
    private User owner;
    private Product product;

    @BeforeEach
    void setUp() {
        wishlistRepository = mock(WishlistItemRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(StoreInventoryRepository.class);
        service = new WishlistService(wishlistRepository, productRepository, inventoryRepository);
        owner = user(7L);
        product = product(21L, 31L);
        when(inventoryRepository.sumStockByVariantIds(any())).thenReturn(List.of());
    }

    @Test
    void addsActiveProductForCurrentUser() {
        when(wishlistRepository.findByUserIdAndProductId(7L, 21L)).thenReturn(Optional.empty());
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(wishlistRepository.save(any(WishlistItem.class))).thenAnswer(invocation -> {
            WishlistItem item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "createdAt", OffsetDateTime.parse("2026-10-13T01:00:00Z"));
            return item;
        });

        var response = service.add(owner, 21L);

        assertThat(response.product().id()).isEqualTo(21L);
        assertThat(response.product().defaultVariantId()).isEqualTo(31L);
        assertThat(response.product().inStock()).isFalse();
        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addingExistingProductDoesNotCreateDuplicate() {
        WishlistItem existing = item(owner, product);
        when(wishlistRepository.findByUserIdAndProductId(7L, 21L)).thenReturn(Optional.of(existing));

        var response = service.add(owner, 21L);

        assertThat(response.product().id()).isEqualTo(21L);
        verify(wishlistRepository, never()).save(any());
        verify(productRepository, never()).findByIdAndStatus(any(), any());
    }

    @Test
    void listsOnlyCurrentUsersItems() {
        WishlistItem existing = item(owner, product);
        when(wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(existing));

        var responses = service.list(owner);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().product().slug()).isEqualTo("ute-mouse");
        verify(wishlistRepository).findAllByUserIdOrderByCreatedAtDesc(7L);
    }

    @Test
    void removesOnlyOwnedProduct() {
        WishlistItem existing = item(owner, product);
        when(wishlistRepository.findByUserIdAndProductId(7L, 21L)).thenReturn(Optional.of(existing));

        service.remove(owner, 21L);

        verify(wishlistRepository).delete(existing);
    }

    @Test
    void hidesAnotherUsersWishlistItem() {
        when(wishlistRepository.findByUserIdAndProductId(7L, 21L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(owner, 21L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("WISHLIST_ITEM_NOT_FOUND");
        verify(wishlistRepository, never()).delete(any());
    }

    @Test
    void rejectsMissingOrInactiveProduct() {
        when(wishlistRepository.findByUserIdAndProductId(7L, 21L)).thenReturn(Optional.empty());
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(owner, 21L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("PRODUCT_NOT_FOUND");
    }

    private User user(Long id) {
        User user = new User("Wishlist User", "wishlist@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Product product(Long productId, Long variantId) {
        Category category = new Category("Mouse", "mouse", null, 1);
        Product product = new Product("UTE Mouse", "ute-mouse", category, null,
                new BigDecimal("790000"), null);
        ProductVariant variant = new ProductVariant("UTE-MOUSE-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(variant);
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(variant, "id", variantId);
        return product;
    }

    private WishlistItem item(User user, Product product) {
        WishlistItem item = new WishlistItem(user, product);
        ReflectionTestUtils.setField(item, "id", 41L);
        ReflectionTestUtils.setField(item, "createdAt", OffsetDateTime.parse("2026-10-13T01:00:00Z"));
        return item;
    }
}
