package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Cart;
import com.utegear.entity.CartItem;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.CartItemRepository;
import com.utegear.repository.CartRepository;
import com.utegear.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CartServiceTest {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProductVariantRepository variantRepository;
    private CartService service;
    private User user;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        variantRepository = mock(ProductVariantRepository.class);
        service = new CartService(cartRepository, cartItemRepository, variantRepository);
        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        variant = activeVariant();
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void addsVariantToCustomersCart() {
        Cart cart = new Cart(user);
        when(variantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(cartRepository.findWithItemsByUserId(7L)).thenReturn(Optional.of(cart));

        Cart result = service.addItem(user, 10L, 2);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItemCount()).isEqualTo(2);
        verify(cartRepository).save(cart);
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> service.addItem(user, 10L, 0))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsVariantNotAttachedToItsProduct() {
        ProductVariant detached = new ProductVariant("DETACHED", Map.of(), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(detached, "id", 11L);
        Product differentProduct = product("Khác", "khac");
        ReflectionTestUtils.setField(detached, "product", differentProduct);
        when(variantRepository.findById(11L)).thenReturn(Optional.of(detached));

        assertThatThrownBy(() -> service.addItem(user, 11L, 1))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VARIANT_UNAVAILABLE");
    }

    @Test
    void cannotUpdateAnotherCustomersItem() {
        when(cartItemRepository.findByIdAndCartUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateQuantity(user, 99L, 2))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("CART_ITEM_NOT_FOUND");
    }

    @Test
    void updatesOwnedItemQuantity() {
        Cart cart = new Cart(user);
        CartItem item = cart.add(variant, 1);
        ReflectionTestUtils.setField(item, "id", 20L);
        when(cartItemRepository.findByIdAndCartUserId(20L, 7L)).thenReturn(Optional.of(item));
        Cart result = service.updateQuantity(user, 20L, 4);

        assertThat(result.getItemCount()).isEqualTo(4);
        verify(cartRepository).save(cart);
    }

    private ProductVariant activeVariant() {
        Product product = product("Bàn phím K87", "ban-phim-k87");
        ProductVariant value = new ProductVariant("K87-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(value);
        ReflectionTestUtils.setField(value, "id", 10L);
        return value;
    }

    private Product product(String name, String slug) {
        return new Product(
                name,
                slug,
                new Category("Bàn phím", "ban-phim", null, 0),
                null,
                new BigDecimal("1000000"),
                null);
    }
}
