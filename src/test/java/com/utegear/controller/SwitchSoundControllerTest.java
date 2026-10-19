package com.utegear.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.catalog.SwitchSoundDtos;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.SwitchSoundService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SwitchSoundControllerTest {

    private CategoryService categoryService;
    private BrandService brandService;
    private ProductCatalogService productService;
    private SwitchSoundService switchSoundService;
    private CatalogController controller;

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryService.class);
        brandService = mock(BrandService.class);
        productService = mock(ProductCatalogService.class);
        switchSoundService = mock(SwitchSoundService.class);
        controller = new CatalogController(categoryService, brandService, productService, switchSoundService);
    }

    @Test
    void switchSounds_delegatesToService() {
        List<SwitchSoundDtos.SwitchProfile> expected = List.of(
                new SwitchSoundDtos.SwitchProfile(
                        "RED_LINEAR", "Red Switch", "Linear", 45, 2.0, 4.0,
                        "Êm", "Mượt mà", "Standard",
                        "https://example.com/red.mp3", "red", "#E53E3E",
                        new SwitchSoundDtos.SynthProfile(180, 0, 0, 95, 1.2),
                        List.of()));
        when(switchSoundService.findAllProfiles()).thenReturn(expected);

        List<SwitchSoundDtos.SwitchProfile> actual = controller.switchSounds();

        assertThat(actual).isSameAs(expected);
        verify(switchSoundService).findAllProfiles();
    }

    @Test
    void productSwitchSounds_delegatesToService() {
        SwitchSoundDtos.ProductSwitchSoundResponse expected = new SwitchSoundDtos.ProductSwitchSoundResponse(
                145L, "Akko Keyboard", "ban-phim", true, "RED_LINEAR", "Disclaimer", List.of());
        when(switchSoundService.forProduct(145L)).thenReturn(expected);

        SwitchSoundDtos.ProductSwitchSoundResponse actual = controller.productSwitchSounds(145L);

        assertThat(actual).isSameAs(expected);
        verify(switchSoundService).forProduct(145L);
    }
}
