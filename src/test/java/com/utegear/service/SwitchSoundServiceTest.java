package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.utegear.dto.catalog.SwitchSoundDtos;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SwitchSoundServiceTest {

    private ProductRepository productRepository;
    private SwitchSoundService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        service = new SwitchSoundService(productRepository);
    }

    @Test
    void findAllProfiles_returnsFiveStandardProfiles() {
        List<SwitchSoundDtos.SwitchProfile> profiles = service.findAllProfiles();

        assertThat(profiles).hasSize(5);
        assertThat(profiles).extracting(SwitchSoundDtos.SwitchProfile::code)
                .containsExactly("RED_LINEAR", "BLUE_CLICKY", "BROWN_TACTILE", "SILVER_SPEED", "SILENT_RED");

        for (SwitchSoundDtos.SwitchProfile profile : profiles) {
            assertThat(profile.audioSecureUrl()).isNotBlank();
            assertThat(profile.audioPublicId()).isNotBlank();
            assertThat(profile.synthProfile()).isNotNull();
            assertThat(profile.actuationForceG()).isPositive();
        }
    }

    @Test
    void forProduct_withKeyboardCategory_matchesVariants() {
        Category category = new Category("Bàn phím", "ban-phim", null, 1);
        Product keyboard = new Product("Bàn phím cơ Akko 3087 DS", "akko-3087", category, null, new BigDecimal("1200000"), null);
        ReflectionTestUtils.setField(keyboard, "id", 145L);

        ProductVariant redVariant = new ProductVariant("AKKO-3087-RED", Map.of("switch", "Red Switch"), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(redVariant, "id", 201L);

        ProductVariant blueVariant = new ProductVariant("AKKO-3087-BLUE", Map.of("switch", "Blue Switch"), false, BigDecimal.ZERO);
        ReflectionTestUtils.setField(blueVariant, "id", 202L);

        keyboard.addVariant(redVariant);
        keyboard.addVariant(blueVariant);

        when(productRepository.findByIdAndStatus(145L, ProductStatus.ACTIVE)).thenReturn(Optional.of(keyboard));

        SwitchSoundDtos.ProductSwitchSoundResponse response = service.forProduct(145L);

        assertThat(response.productId()).isEqualTo(145L);
        assertThat(response.supported()).isTrue();
        assertThat(response.defaultSwitchCode()).isEqualTo("RED_LINEAR");
        assertThat(response.profiles()).hasSize(5);

        SwitchSoundDtos.SwitchProfile redProfile = response.profiles().stream()
                .filter(p -> p.code().equals("RED_LINEAR")).findFirst().orElseThrow();
        assertThat(redProfile.matchedVariantIds()).containsExactly(201L);

        SwitchSoundDtos.SwitchProfile blueProfile = response.profiles().stream()
                .filter(p -> p.code().equals("BLUE_CLICKY")).findFirst().orElseThrow();
        assertThat(blueProfile.matchedVariantIds()).containsExactly(202L);
    }

    @Test
    void forProduct_withNonKeyboard_returnsUnsupported() {
        Category category = new Category("Chuột", "chuot", null, 2);
        Product mouse = new Product("Chuột Logitech G102", "g102", category, null, new BigDecimal("450000"), null);
        ReflectionTestUtils.setField(mouse, "id", 102L);

        when(productRepository.findByIdAndStatus(102L, ProductStatus.ACTIVE)).thenReturn(Optional.of(mouse));

        SwitchSoundDtos.ProductSwitchSoundResponse response = service.forProduct(102L);

        assertThat(response.productId()).isEqualTo(102L);
        assertThat(response.supported()).isFalse();
        assertThat(response.defaultSwitchCode()).isNull();
        assertThat(response.profiles()).isEmpty();
    }

    @Test
    void forProduct_notFound_throwsApiException() {
        when(productRepository.findByIdAndStatus(999L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.forProduct(999L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("PRODUCT_NOT_FOUND");
    }
}
