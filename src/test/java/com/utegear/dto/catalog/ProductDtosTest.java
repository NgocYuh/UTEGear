package com.utegear.dto.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductDtosTest {

    @Test
    void aggregatesProductStockFromActiveVariants() {
        Category category = new Category("Chuột", "chuot", null, 1);
        Product product = new Product(
                "Chuột UTEGear", "chuot-utegear", category, null,
                BigDecimal.valueOf(500_000), null);
        ProductVariant defaultVariant = new ProductVariant(
                "MOUSE-BLACK", Map.of("color", "black"), true, BigDecimal.ZERO);
        ProductVariant whiteVariant = new ProductVariant(
                "MOUSE-WHITE", Map.of("color", "white"), false, BigDecimal.valueOf(20_000));
        ReflectionTestUtils.setField(defaultVariant, "id", 11L);
        ReflectionTestUtils.setField(whiteVariant, "id", 12L);
        product.addVariant(defaultVariant);
        product.addVariant(whiteVariant);

        ProductDtos.Summary result = ProductDtos.Summary.from(
                product, Map.of(11L, 3L, 12L, 5L));

        assertThat(result.totalStock()).isEqualTo(8);
        assertThat(result.inStock()).isTrue();
        assertThat(result.variants()).extracting(ProductDtos.Variant::totalStock)
                .containsExactly(3L, 5L);
    }
}
