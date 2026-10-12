package com.utegear.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CartTest {

    private Cart cart;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        User user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        Product product = new Product(
                "Bàn phím K87",
                "ban-phim-k87",
                new Category("Bàn phím", "ban-phim", null, 0),
                null,
                new BigDecimal("1000000"),
                new BigDecimal("900000"));
        variant = new ProductVariant(
                "K87-BLACK",
                Map.of("color", "black"),
                true,
                new BigDecimal("50000"));
        product.addVariant(variant);
        ReflectionTestUtils.setField(variant, "id", 10L);
        cart = new Cart(user);
    }

    @Test
    void addingSameVariantIncreasesExistingLine() {
        CartItem first = cart.add(variant, 2);
        CartItem second = cart.add(variant, 3);

        assertThat(second).isSameAs(first);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(first.getQuantity()).isEqualTo(5);
        assertThat(cart.getItemCount()).isEqualTo(5);
    }

    @Test
    void calculatesSubtotalFromEffectivePriceAndVariantAdjustment() {
        cart.add(variant, 3);

        assertThat(cart.getSubtotal()).isEqualByComparingTo("2850000");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> cart.add(variant, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Số lượng phải lớn hơn 0");
    }
}
