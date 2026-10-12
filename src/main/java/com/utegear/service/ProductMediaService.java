package com.utegear.service;

import com.utegear.dto.media.ProductImageResponse;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.exception.ApiException;
import com.utegear.media.MediaStorage;
import com.utegear.media.UploadedMedia;
import com.utegear.repository.ProductImageRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.util.TransactionCallbacks;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductMediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final MediaStorage mediaStorage;
    private final TransactionCallbacks transactionCallbacks;
    private final String baseFolder;
    private final long maxFileSize;

    public ProductMediaService(
            ProductRepository productRepository,
            ProductImageRepository imageRepository,
            MediaStorage mediaStorage,
            TransactionCallbacks transactionCallbacks,
            @Value("${cloudinary.folder:utegear}") String baseFolder,
            @Value("${cloudinary.max-file-size:5242880}") long maxFileSize) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.mediaStorage = mediaStorage;
        this.transactionCallbacks = transactionCallbacks;
        this.baseFolder = baseFolder;
        this.maxFileSize = maxFileSize;
    }

    @Transactional
    public ProductImageResponse upload(
            Long productId,
            MultipartFile file,
            String altText,
            boolean primary) {
        Product product = requireProduct(productId);
        validate(file);
        UploadedMedia uploaded = mediaStorage.upload(file, folder(productId));
        transactionCallbacks.afterRollback(() -> mediaStorage.delete(uploaded.publicId()));

        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId);
        if (primary) {
            clearPrimary(images);
        }
        int sortOrder = images.stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        ProductImage image = new ProductImage(
                uploaded.secureUrl(), uploaded.publicId(), altText, sortOrder, primary);
        product.addImage(image);
        return ProductImageResponse.from(imageRepository.save(image));
    }

    @Transactional
    public ProductImageResponse update(
            Long productId,
            Long imageId,
            MultipartFile file,
            String altText,
            boolean primary) {
        requireProduct(productId);
        ProductImage image = requireImage(productId, imageId);
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId);
        if (primary) {
            clearPrimary(images);
        }

        if (file == null || file.isEmpty()) {
            image.updateMetadata(altText, primary);
            return ProductImageResponse.from(imageRepository.save(image));
        }

        validate(file);
        UploadedMedia uploaded = mediaStorage.upload(file, folder(productId));
        String oldPublicId = image.getPublicId();
        transactionCallbacks.afterRollback(() -> mediaStorage.delete(uploaded.publicId()));
        transactionCallbacks.afterCommit(() -> mediaStorage.delete(oldPublicId));
        image.updateMedia(uploaded.secureUrl(), uploaded.publicId(), altText, primary);
        return ProductImageResponse.from(imageRepository.save(image));
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        requireProduct(productId);
        ProductImage image = requireImage(productId, imageId);
        String publicId = image.getPublicId();
        imageRepository.delete(image);
        transactionCallbacks.afterCommit(() -> mediaStorage.delete(publicId));
    }

    @Transactional
    public List<ProductImageResponse> reorder(Long productId, List<Long> imageIds) {
        requireProduct(productId);
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId);
        validateOrder(images, imageIds);

        int temporaryBase = images.stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        for (int index = 0; index < images.size(); index++) {
            images.get(index).setTemporarySortOrder(temporaryBase + index);
        }
        imageRepository.flush();

        for (int index = 0; index < imageIds.size(); index++) {
            Long imageId = imageIds.get(index);
            ProductImage image = images.stream()
                    .filter(candidate -> candidate.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(this::validationFailed);
            image.setSortOrder(index);
        }
        imageRepository.flush();
        return imageIds.stream()
                .map(id -> images.stream().filter(image -> image.getId().equals(id)).findFirst().orElseThrow())
                .map(ProductImageResponse::from)
                .toList();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "File ảnh là bắt buộc");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "INVALID_MEDIA_TYPE", "Chỉ chấp nhận JPEG, PNG hoặc WebP");
        }
        if (file.getSize() > maxFileSize) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "File ảnh vượt quá dung lượng cho phép");
        }
    }

    private void clearPrimary(List<ProductImage> images) {
        images.stream().filter(ProductImage::isPrimary).forEach(image -> image.setPrimary(false));
    }

    private void validateOrder(List<ProductImage> images, List<Long> imageIds) {
        Set<Long> requestedIds = new HashSet<>(imageIds);
        Set<Long> existingIds = images.stream().map(ProductImage::getId).collect(java.util.stream.Collectors.toSet());
        if (imageIds.size() != images.size()
                || requestedIds.size() != imageIds.size()
                || !requestedIds.equals(existingIds)) {
            throw validationFailed();
        }
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
    }

    private ProductImage requireImage(Long productId, Long imageId) {
        return imageRepository.findByIdAndProductId(imageId, productId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "PRODUCT_IMAGE_NOT_FOUND", "Không tìm thấy ảnh sản phẩm"));
    }

    private String folder(Long productId) {
        return baseFolder + "/products/" + productId;
    }

    private ApiException validationFailed() {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Thứ tự ảnh không hợp lệ");
    }
}
