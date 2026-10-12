package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.importer.ProductImportRecord;
import com.utegear.dto.importer.VariantImportRecord;
import com.utegear.entity.Brand;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.exception.ApiException;
import com.utegear.importer.SourceImage;
import com.utegear.importer.SourceImageFetcher;
import com.utegear.media.MediaStorage;
import com.utegear.media.UploadedMedia;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.util.TransactionCallbacks;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ProductImportRowServiceTest {

    private ProductRepository products;
    private ProductVariantRepository variants;
    private CategoryRepository categories;
    private BrandRepository brands;
    private SourceImageFetcher imageFetcher;
    private MediaStorage mediaStorage;
    private ProductImportRowService service;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        variants = mock(ProductVariantRepository.class);
        categories = mock(CategoryRepository.class);
        brands = mock(BrandRepository.class);
        imageFetcher = mock(SourceImageFetcher.class);
        mediaStorage = mock(MediaStorage.class);
        service = new ProductImportRowService(
                products,
                variants,
                categories,
                brands,
                imageFetcher,
                mediaStorage,
                new TransactionCallbacks());

        Category category = new Category("Bàn phím", "ban-phim", null, 1);
        Brand brand = new Brand("E-Dra", "e-dra", null);
        when(categories.findBySlug("ban-phim")).thenReturn(Optional.of(category));
        when(brands.findByNameIgnoreCase("E-Dra")).thenReturn(Optional.of(brand));
        when(imageFetcher.fetch(any(URI.class)))
                .thenReturn(new SourceImage(new byte[]{1, 2, 3}, "image/webp", "keyboard.webp"));
        when(mediaStorage.upload(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(new UploadedMedia("https://res.cloudinary.com/demo/keyboard.webp", "utegear/keyboard"));
        when(products.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ReflectionTestUtils.setField(product, "id", 101L);
            return product;
        });
        when(products.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void importsValidProductWithCloudinaryImage() {
        Long id = service.importRecord(validRecord());

        assertThat(id).isEqualTo(101L);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(products).save(productCaptor.capture());
        Product product = productCaptor.getValue();
        assertThat(product.getSlug()).isEqualTo("ban-phim-co-e-dra-ek384");
        assertThat(product.getSpecifications().get("sourceName").asText()).isEqualTo("gearvn");
        assertThat(product.getSpecifications().get("sourceUrl").asText()).contains("gearvn.com/products/");
        assertThat(product.getVariants()).singleElement().satisfies(variant -> {
            assertThat(variant.getSku()).isEqualTo("EDRA-EK384-BROWN");
            assertThat(variant.isDefault()).isTrue();
            assertThat(variant.getOptionValues()).isEmpty();
        });
        assertThat(product.getImages()).singleElement().satisfies(image -> {
            assertThat(image.getPublicId()).isEqualTo("utegear/keyboard");
            assertThat(image.isPrimary()).isTrue();
        });
    }

    @Test
    void createsDefaultVariantForSingleVariantWithoutOptions() {
        ProductImportRecord record = withVariants(List.of(new VariantImportRecord(
                "EDRA-EK384-BROWN", Map.of(), new BigDecimal("1290000"), false)));

        service.importRecord(record);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(products).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getVariants().getFirst().isDefault()).isTrue();
    }

    @Test
    void rejectsProductWithoutVariants() {
        assertCode(withVariants(List.of()), "VARIANTS_REQUIRED");
        verify(products, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateSku() {
        when(variants.existsBySku("EDRA-EK384-BROWN")).thenReturn(true);

        assertCode(validRecord(), "SKU_DUPLICATE");
        verify(products, never()).saveAndFlush(any());
    }

    @Test
    void rejectsInvalidVariantPrice() {
        ProductImportRecord record = withVariants(List.of(new VariantImportRecord(
                "EDRA-EK384-BROWN", Map.of(), BigDecimal.ZERO, true)));

        assertCode(record, "INVALID_PRICE");
    }

    @Test
    void rejectsUnknownBrand() {
        when(brands.findByNameIgnoreCase("E-Dra")).thenReturn(Optional.empty());

        assertCode(validRecord(), "BRAND_NOT_FOUND");
        verify(imageFetcher, never()).fetch(any());
    }

    @Test
    void rejectsMissingRequiredField() {
        ProductImportRecord valid = validRecord();
        ProductImportRecord missingBrand = new ProductImportRecord(
                valid.sourceName(), valid.sourceUrl(), valid.crawledAt(), valid.name(), valid.description(),
                " ", valid.category(), valid.basePrice(), valid.imageUrls(), valid.variants());

        assertCode(missingBrand, "VALIDATION_FAILED");
        verify(products, never()).saveAndFlush(any());
    }

    private void assertCode(ProductImportRecord record, String expectedCode) {
        assertThatThrownBy(() -> service.importRecord(record))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(expectedCode);
    }

    private ProductImportRecord withVariants(List<VariantImportRecord> variants) {
        ProductImportRecord valid = validRecord();
        return new ProductImportRecord(
                valid.sourceName(), valid.sourceUrl(), valid.crawledAt(), valid.name(), valid.description(),
                valid.brand(), valid.category(), valid.basePrice(), valid.imageUrls(), variants);
    }

    private ProductImportRecord validRecord() {
        return new ProductImportRecord(
                "gearvn",
                "https://gearvn.com/products/ban-phim-co-e-dra-ek384",
                "2026-10-12T08:34:07Z",
                "Bàn phím cơ E-Dra EK384",
                "Bàn phím cơ layout 84 phím.",
                "E-Dra",
                "ban-phim",
                new BigDecimal("1290000"),
                List.of("https://product.hstatic.net/sample/ek384.webp"),
                List.of(new VariantImportRecord(
                        "EDRA-EK384-BROWN", Map.of(), new BigDecimal("1290000"), true)));
    }
}
