package com.utegear.service;

import com.utegear.dto.importer.ProductImportRecord;
import com.utegear.dto.importer.VariantImportRecord;
import com.utegear.entity.Brand;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.importer.SourceImage;
import com.utegear.importer.SourceImageFetcher;
import com.utegear.media.MediaStorage;
import com.utegear.media.UploadedMedia;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.util.Slugifier;
import com.utegear.util.TransactionCallbacks;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImportRowService implements ProductRowImporter {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "ban-phim", "chuot", "tai-nghe", "keycap", "switch", "phu-kien");

    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final SourceImageFetcher imageFetcher;
    private final MediaStorage mediaStorage;
    private final TransactionCallbacks transactionCallbacks;

    public ProductImportRowService(
            ProductRepository products,
            ProductVariantRepository variants,
            CategoryRepository categories,
            BrandRepository brands,
            SourceImageFetcher imageFetcher,
            MediaStorage mediaStorage,
            TransactionCallbacks transactionCallbacks) {
        this.products = products;
        this.variants = variants;
        this.categories = categories;
        this.brands = brands;
        this.imageFetcher = imageFetcher;
        this.mediaStorage = mediaStorage;
        this.transactionCallbacks = transactionCallbacks;
    }

    @Transactional
    @Override
    public Long importRecord(ProductImportRecord record) {
        validateRecord(record);
        Category category = categories.findBySlug(record.category()).orElseThrow(() -> invalid(
                "CATEGORY_NOT_FOUND", "Danh mục không tồn tại: " + record.category()));
        if (!category.isActive()) {
            throw invalid("CATEGORY_INACTIVE", "Danh mục đã ngừng hoạt động: " + record.category());
        }
        Brand brand = brands.findByNameIgnoreCase(record.brand()).orElseThrow(() -> invalid(
                "BRAND_NOT_FOUND", "Thương hiệu không tồn tại: " + record.brand()));
        if (!brand.isActive()) {
            throw invalid("BRAND_INACTIVE", "Thương hiệu đã ngừng hoạt động: " + record.brand());
        }

        String slug = Slugifier.slugify(record.name());
        if (slug.isBlank() || products.existsBySlug(slug)) {
            throw invalid("PRODUCT_DUPLICATE", "Tên hoặc slug sản phẩm đã tồn tại");
        }

        Product product = new Product(
                record.name().trim(),
                slug,
                category,
                brand,
                record.basePrice(),
                null);
        ObjectNode provenance = JsonNodeFactory.instance.objectNode();
        provenance.put("sourceName", record.sourceName().trim());
        provenance.put("sourceUrl", record.sourceUrl().trim());
        provenance.put("crawledAt", record.crawledAt().trim());
        ObjectNode technical = provenance.putObject("technicalSpecifications");
        if (record.specifications() != null) {
            record.specifications().forEach((label, value) -> {
                if (label == null || label.isBlank() || value == null || value.isBlank()
                        || label.length() > 200 || value.length() > 4000) {
                    throw invalid("INVALID_SPECIFICATIONS", "Nhãn/giá trị thông số không hợp lệ");
                }
                technical.put(label.trim(), value.trim());
            });
        }
        product.update(product.getName(), product.getSlug(), category, brand,
                record.description() == null ? null : record.description().strip(),
                record.basePrice(), null, product.getStatus(), provenance);
        addVariants(product, record);
        Product saved = products.saveAndFlush(product);
        addImages(saved, record.imageUrls());
        return products.save(saved).getId();
    }

    private void addVariants(Product product, ProductImportRecord record) {
        boolean singleWithoutOptions = record.variants().size() == 1
                && record.variants().getFirst() != null
                && normalizedOptions(record.variants().getFirst()).isEmpty();
        Set<String> rowSkus = new HashSet<>();
        int defaultCount = 0;
        for (VariantImportRecord input : record.variants()) {
            if (input == null) {
                throw invalid("INVALID_VARIANT", "Biến thể không được rỗng");
            }
            String sku = required(input.sku(), "SKU là bắt buộc").toUpperCase();
            if (!rowSkus.add(sku) || variants.existsBySku(sku)) {
                throw invalid("SKU_DUPLICATE", "SKU đã tồn tại hoặc bị trùng trong file: " + sku);
            }
            if (input.price() == null || input.price().signum() <= 0) {
                throw invalid("INVALID_PRICE", "Giá biến thể phải lớn hơn 0: " + sku);
            }
            boolean isDefault = singleWithoutOptions || Boolean.TRUE.equals(input.defaultVariant());
            defaultCount += isDefault ? 1 : 0;
            product.addVariant(new ProductVariant(
                    sku,
                    normalizedOptions(input),
                    isDefault,
                    input.price().subtract(record.basePrice())));
        }
        if (defaultCount != 1) {
            throw invalid("INVALID_DEFAULT_VARIANT", "Sản phẩm phải có đúng một biến thể mặc định");
        }
    }

    private void addImages(Product product, List<String> imageUrls) {
        for (int index = 0; index < imageUrls.size(); index++) {
            URI source;
            try {
                source = URI.create(imageUrls.get(index));
            } catch (IllegalArgumentException exception) {
                throw invalid("INVALID_IMAGE_URL", "URL ảnh không hợp lệ");
            }
            SourceImage image = imageFetcher.fetch(source);
            UploadedMedia uploaded = mediaStorage.upload(
                    image.content(),
                    image.contentType(),
                    image.originalFilename(),
                    "utegear/products/" + product.getId());
            transactionCallbacks.afterRollback(() -> mediaStorage.delete(uploaded.publicId()));
            product.addImage(new ProductImage(
                    uploaded.secureUrl(),
                    uploaded.publicId(),
                    product.getName(),
                    index,
                    index == 0));
        }
    }

    private void validateRecord(ProductImportRecord record) {
        if (record == null) {
            throw invalid("INVALID_RECORD", "Dòng dữ liệu không được rỗng");
        }
        required(record.sourceName(), "source_name là bắt buộc");
        requireHttpUrl(record.sourceUrl(), "source_url");
        required(record.name(), "name là bắt buộc");
        required(record.brand(), "brand là bắt buộc");
        String category = required(record.category(), "category là bắt buộc");
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw invalid("INVALID_CATEGORY", "Danh mục không được hỗ trợ: " + category);
        }
        if (record.basePrice() == null || record.basePrice().signum() <= 0) {
            throw invalid("INVALID_PRICE", "base_price phải lớn hơn 0");
        }
        try {
            OffsetDateTime.parse(required(record.crawledAt(), "crawled_at là bắt buộc"));
        } catch (DateTimeParseException exception) {
            throw invalid("INVALID_CRAWLED_AT", "crawled_at phải là thời gian ISO-8601 có múi giờ");
        }
        if (record.variants() == null || record.variants().isEmpty()) {
            throw invalid("VARIANTS_REQUIRED", "Sản phẩm phải có ít nhất một biến thể");
        }
        if (record.imageUrls() == null || record.imageUrls().isEmpty()) {
            throw invalid("IMAGES_REQUIRED", "Sản phẩm phải có ít nhất một URL ảnh");
        }
        record.imageUrls().forEach(url -> requireHttpUrl(url, "image_urls"));
    }

    private Map<String, String> normalizedOptions(VariantImportRecord variant) {
        if (variant.optionValues() == null) {
            return Map.of();
        }
        if (variant.optionValues().entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue().isBlank())) {
            throw invalid("INVALID_VARIANT_OPTIONS", "Tên và giá trị lựa chọn biến thể không được rỗng");
        }
        return variant.optionValues();
    }

    private void requireHttpUrl(String value, String field) {
        try {
            URI uri = URI.create(required(value, field + " là bắt buộc"));
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw invalid("INVALID_URL", field + " phải là URL HTTP(S) hợp lệ");
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw invalid("VALIDATION_FAILED", message);
        }
        return value.trim();
    }

    private ApiException invalid(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }
}
