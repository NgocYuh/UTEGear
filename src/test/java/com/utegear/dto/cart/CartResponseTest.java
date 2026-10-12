package com.utegear.dto.cart;

import static org.assertj.core.api.Assertions.assertThat;

import com.utegear.entity.Cart;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CartResponseTest {

    @Test
    void mapsContractFieldsAndAggregatedAvailability() {
        Product product = new Product(
                "Bàn phím K87",
                "ban-phim-k87",
                new Category("Bàn phím", "ban-phim", null, 0),
                null,
                new BigDecimal("1000000"),
                new BigDecimal("900000"));
        ProductVariant variant = new ProductVariant(
                "K87-BLACK",
                Map.of("color", "black"),
                true,
                new BigDecimal("50000"));
        ProductImage image = new ProductImage(
                "https://res.cloudinary.com/demo/k87.jpg",
                "utegear/k87",
                "Bàn phím K87",
                0,
                true);
        product.addVariant(variant);
        product.addImage(image);
        ReflectionTestUtils.setField(product, "id", 101L);
        ReflectionTestUtils.setField(variant, "id", 1001L);
        ReflectionTestUtils.setField(image, "id", 501L);

        User user = new User("Cart User", "cart@example.test", null, "hash");
        Cart cart = new Cart(user);
        cart.add(variant, 2);
        ReflectionTestUtils.setField(cart, "id", 301L);
        ReflectionTestUtils.setField(cart.getItems().getFirst(), "id", 401L);

        CartResponse response = CartResponse.from(cart, Map.of(1001L, 6L));

        assertThat(response.id()).isEqualTo(301L);
        assertThat(response.itemCount()).isEqualTo(2);
        assertThat(response.subtotal()).isEqualByComparingTo("1900000");
        assertThat(response.items().getFirst().variantId()).isEqualTo(1001L);
        assertThat(response.items().getFirst().availableStock()).isEqualTo(6L);
        assertThat(response.items().getFirst().available()).isTrue();
        assertThat(response.items().getFirst().product().primaryImage().secureUrl())
                .isEqualTo("https://res.cloudinary.com/demo/k87.jpg");
    }
}
