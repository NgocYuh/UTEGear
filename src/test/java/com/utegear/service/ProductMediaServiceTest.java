package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.media.ProductImageResponse;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.exception.ApiException;
import com.utegear.media.MediaStorage;
import com.utegear.media.UploadedMedia;
import com.utegear.repository.ProductImageRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.util.TransactionCallbacks;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class ProductMediaServiceTest {

    private ProductRepository products;
    private ProductImageRepository images;
    private MediaStorage storage;
    private ProductMediaService service;
    private Product product;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        images = mock(ProductImageRepository.class);
        storage = mock(MediaStorage.class);
        service = new ProductMediaService(
                products, images, storage, new TransactionCallbacks(), "utegear-test", 5_242_880);
        product = product(102L);
        when(products.findById(102L)).thenReturn(Optional.of(product));
        when(images.save(any(ProductImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadsImageAndStoresCloudinaryIdentifiers() {
        MockMultipartFile file = image("keyboard.webp", "image/webp", new byte[]{1, 2, 3});
        when(storage.upload(file, "utegear-test/products/102"))
                .thenReturn(new UploadedMedia("https://res.cloudinary.com/demo/image.webp", "utegear/102/image"));
        when(images.findByProductIdOrderBySortOrderAsc(102L)).thenReturn(List.of());

        ProductImageResponse result = service.upload(102L, file, " Bàn phím K87 ", true);

        assertThat(result.secureUrl()).startsWith("https://res.cloudinary.com/");
        assertThat(result.publicId()).isEqualTo("utegear/102/image");
        assertThat(result.altText()).isEqualTo("Bàn phím K87");
        assertThat(result.primary()).isTrue();
        assertThat(result.sortOrder()).isZero();
    }

    @Test
    void rejectsUnsupportedMimeTypeBeforeUpload() {
        MockMultipartFile file = image("payload.svg", "image/svg+xml", new byte[]{1});

        assertThatThrownBy(() -> service.upload(102L, file, null, false))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("INVALID_MEDIA_TYPE");
        verify(storage, never()).upload(any(), any());
    }

    @Test
    void rejectsFileAboveConfiguredLimit() {
        ProductMediaService limited = new ProductMediaService(
                products, images, storage, new TransactionCallbacks(), "utegear-test", 2);
        MockMultipartFile file = image("large.png", "image/png", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> limited.upload(102L, file, null, false))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("FILE_TOO_LARGE");
        verify(storage, never()).upload(any(), any());
    }

    @Test
    void replacesImageAndDeletesOldAssetAfterCommit() {
        ProductImage existing = imageEntity(501L, "old-url", "old-public-id", 0, true);
        MockMultipartFile replacement = image("new.jpg", "image/jpeg", new byte[]{4, 5});
        when(images.findByIdAndProductId(501L, 102L)).thenReturn(Optional.of(existing));
        when(images.findByProductIdOrderBySortOrderAsc(102L)).thenReturn(List.of(existing));
        when(storage.upload(replacement, "utegear-test/products/102"))
                .thenReturn(new UploadedMedia("new-url", "new-public-id"));

        ProductImageResponse result = service.update(102L, 501L, replacement, "Ảnh mới", true);

        assertThat(result.publicId()).isEqualTo("new-public-id");
        verify(storage).delete("old-public-id");
    }

    @Test
    void updatesMetadataWithoutUploadingReplacement() {
        ProductImage existing = imageEntity(501L, "url", "public-id", 0, false);
        when(images.findByIdAndProductId(501L, 102L)).thenReturn(Optional.of(existing));
        when(images.findByProductIdOrderBySortOrderAsc(102L)).thenReturn(List.of(existing));

        ProductImageResponse result = service.update(102L, 501L, null, "Ảnh chính", true);

        assertThat(result.primary()).isTrue();
        verify(storage, never()).upload(any(), any());
        verify(storage, never()).delete(any());
    }

    @Test
    void deletesDatabaseImageAndCloudinaryAsset() {
        ProductImage existing = imageEntity(501L, "url", "public-id", 0, true);
        when(images.findByIdAndProductId(501L, 102L)).thenReturn(Optional.of(existing));

        service.delete(102L, 501L);

        verify(images).delete(existing);
        verify(storage).delete("public-id");
    }

    @Test
    void reordersEveryImageWithoutChangingMembership() {
        ProductImage first = imageEntity(501L, "url-1", "id-1", 0, true);
        ProductImage second = imageEntity(502L, "url-2", "id-2", 1, false);
        when(images.findByProductIdOrderBySortOrderAsc(102L)).thenReturn(List.of(first, second));

        List<ProductImageResponse> result = service.reorder(102L, List.of(502L, 501L));

        assertThat(result).extracting(ProductImageResponse::id).containsExactly(502L, 501L);
        assertThat(second.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        verify(images, times(2)).flush();
    }

    @Test
    void rejectsIncompleteImageOrder() {
        ProductImage first = imageEntity(501L, "url-1", "id-1", 0, true);
        ProductImage second = imageEntity(502L, "url-2", "id-2", 1, false);
        when(images.findByProductIdOrderBySortOrderAsc(102L)).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> service.reorder(102L, List.of(501L)))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    private MockMultipartFile image(String name, String contentType, byte[] bytes) {
        return new MockMultipartFile("file", name, contentType, bytes);
    }

    private ProductImage imageEntity(
            Long id, String url, String publicId, int sortOrder, boolean primary) {
        ProductImage image = new ProductImage(url, publicId, null, sortOrder, primary);
        product.addImage(image);
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }

    private Product product(Long id) {
        Product value = new Product("Bàn phím K87", "ban-phim-k87",
                new Category("Bàn phím", "ban-phim", null, 1), null,
                BigDecimal.valueOf(900_000), null);
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }
}
