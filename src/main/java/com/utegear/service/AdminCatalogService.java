package com.utegear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.Brand;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.util.Slugifier;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogService {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreInventoryRepository inventoryRepository;

    public AdminCatalogService(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            StoreInventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.CategoryView> categories() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(AdminDtos.CategoryView::from).toList();
    }

    @Transactional
    public AdminDtos.CategoryView createCategory(AdminDtos.CategoryRequest request) {
        String name = request.name().trim();
        String slug = Slugifier.slugify(name);
        requireUniqueCategory(name, slug, null);
        Category category = new Category(name, slug, normalize(request.description()), request.sortOrder());
        category.update(name, slug, normalize(request.description()), request.sortOrder(), request.active());
        return AdminDtos.CategoryView.from(saveCategory(category));
    }

    @Transactional
    public AdminDtos.CategoryView updateCategory(Long id, AdminDtos.CategoryRequest request) {
        Category category = requireCategory(id);
        String name = request.name().trim();
        String slug = Slugifier.slugify(name);
        requireUniqueCategory(name, slug, id);
        category.update(name, slug, normalize(request.description()), request.sortOrder(), request.active());
        return AdminDtos.CategoryView.from(saveCategory(category));
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.BrandView> brands() {
        return brandRepository.findAllByOrderByNameAsc().stream().map(AdminDtos.BrandView::from).toList();
    }

    @Transactional
    public AdminDtos.BrandView createBrand(AdminDtos.BrandRequest request) {
        String name = request.name().trim();
        String slug = Slugifier.slugify(name);
        requireUniqueBrand(name, slug, null);
        Brand brand = new Brand(name, slug, normalize(request.description()));
        brand.update(name, slug, normalize(request.description()), request.active());
        return AdminDtos.BrandView.from(saveBrand(brand));
    }

    @Transactional
    public AdminDtos.BrandView updateBrand(Long id, AdminDtos.BrandRequest request) {
        Brand brand = requireBrand(id);
        String name = request.name().trim();
        String slug = Slugifier.slugify(name);
        requireUniqueBrand(name, slug, id);
        brand.update(name, slug, normalize(request.description()), request.active());
        return AdminDtos.BrandView.from(saveBrand(brand));
    }

    @Transactional(readOnly = true)
    public AdminDtos.ProductPage products(String query, ProductStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw validation("Phân trang không hợp lệ");
        }
        Specification<Product> specification = (root, ignored, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("name")),
                        "%" + query.trim().toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        Page<Product> products = productRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        Map<Long, Long> stock = stock(products.getContent());
        List<AdminDtos.ProductView> content = products.getContent().stream()
                .map(product -> AdminDtos.ProductView.from(product, product.getVariants().stream()
                        .map(ProductVariant::getId).mapToLong(id -> stock.getOrDefault(id, 0L)).sum()))
                .toList();
        return new AdminDtos.ProductPage(content, products.getNumber(), products.getSize(),
                products.getTotalElements(), products.getTotalPages(), products.isFirst(),
                products.isLast(), products.isEmpty());
    }

    @Transactional(readOnly = true)
    public AdminDtos.ProductView product(Long id) {
        Product product = requireProduct(id);
        Map<Long, Long> stock = stock(List.of(product));
        long totalStock = product.getVariants().stream().map(ProductVariant::getId)
                .mapToLong(variantId -> stock.getOrDefault(variantId, 0L)).sum();
        return AdminDtos.ProductView.from(product, totalStock);
    }

    @Transactional
    public AdminDtos.ProductView createProduct(AdminDtos.ProductRequest request) {
        validateProduct(request);
        String name = request.name().trim();
        String slug = uniqueProductSlug(Slugifier.slugify(name), null);
        String sku = request.defaultSku().trim().toUpperCase();
        if (variantRepository.existsBySku(sku)) {
            throw conflict("SKU_ALREADY_EXISTS", "SKU đã tồn tại");
        }
        Product product = new Product(name, slug, requireCategory(request.categoryId()),
                optionalBrand(request.brandId()), request.basePrice(), request.salePrice());
        product.update(name, slug, product.getCategory(), product.getBrand(), normalize(request.description()),
                request.basePrice(), request.salePrice(), request.status(), specifications(request.specifications()));
        product.addVariant(new ProductVariant(sku, request.optionValues(), true, BigDecimal.ZERO));
        return AdminDtos.ProductView.from(saveProduct(product), 0);
    }

    @Transactional
    public AdminDtos.ProductView updateProduct(Long id, AdminDtos.ProductRequest request) {
        validateProduct(request);
        Product product = requireProduct(id);
        ProductVariant defaultVariant = product.getVariants().stream().filter(ProductVariant::isDefault)
                .findFirst().orElseThrow(() -> conflict("DEFAULT_VARIANT_MISSING", "Sản phẩm thiếu biến thể mặc định"));
        String sku = request.defaultSku().trim().toUpperCase();
        if (variantRepository.existsBySkuAndIdNot(sku, defaultVariant.getId())) {
            throw conflict("SKU_ALREADY_EXISTS", "SKU đã tồn tại");
        }
        String name = request.name().trim();
        String slug = uniqueProductSlug(Slugifier.slugify(name), id);
        product.update(name, slug, requireCategory(request.categoryId()), optionalBrand(request.brandId()),
                normalize(request.description()), request.basePrice(), request.salePrice(), request.status(),
                specifications(request.specifications()));
        defaultVariant.update(sku, request.optionValues(), defaultVariant.getPriceAdjustment(), true);
        Product saved = saveProduct(product);
        return product(saved.getId());
    }

    @Transactional
    public AdminDtos.ProductView changeProductStatus(Long id, ProductStatus status) {
        Product product = requireProduct(id);
        product.changeStatus(status);
        Product saved = saveProduct(product);
        return product(saved.getId());
    }

    private Map<Long, Long> stock(List<Product> products) {
        List<Long> variantIds = products.stream().flatMap(product -> product.getVariants().stream())
                .map(ProductVariant::getId).filter(id -> id != null).toList();
        if (variantIds.isEmpty()) {
            return Map.of();
        }
        return inventoryRepository.sumStockByVariantIds(variantIds).stream()
                .collect(Collectors.toMap(StoreInventoryRepository.VariantStock::getVariantId,
                        StoreInventoryRepository.VariantStock::getQuantity));
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Không tìm thấy danh mục"));
    }

    private Brand requireBrand(Long id) {
        return brandRepository.findById(id).orElseThrow(() -> notFound("BRAND_NOT_FOUND", "Không tìm thấy thương hiệu"));
    }

    private Brand optionalBrand(Long id) {
        return id == null ? null : requireBrand(id);
    }

    private Product requireProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
    }

    private void requireUniqueCategory(String name, String slug, Long id) {
        boolean duplicate = id == null
                ? categoryRepository.existsByNameIgnoreCase(name) || categoryRepository.existsBySlug(slug)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id) || categoryRepository.existsBySlugAndIdNot(slug, id);
        if (duplicate) throw conflict("CATEGORY_ALREADY_EXISTS", "Danh mục đã tồn tại");
    }

    private void requireUniqueBrand(String name, String slug, Long id) {
        boolean duplicate = id == null
                ? brandRepository.existsByNameIgnoreCase(name) || brandRepository.existsBySlug(slug)
                : brandRepository.existsByNameIgnoreCaseAndIdNot(name, id) || brandRepository.existsBySlugAndIdNot(slug, id);
        if (duplicate) throw conflict("BRAND_ALREADY_EXISTS", "Thương hiệu đã tồn tại");
    }

    private String uniqueProductSlug(String slug, Long id) {
        boolean duplicate = id == null ? productRepository.existsBySlug(slug) : productRepository.existsBySlugAndIdNot(slug, id);
        if (duplicate) throw conflict("PRODUCT_ALREADY_EXISTS", "Tên sản phẩm tạo slug đã tồn tại");
        return slug;
    }

    private Category saveCategory(Category category) {
        try { return categoryRepository.save(category); }
        catch (DataIntegrityViolationException exception) { throw conflict("CATEGORY_ALREADY_EXISTS", "Danh mục đã tồn tại"); }
    }

    private Brand saveBrand(Brand brand) {
        try { return brandRepository.save(brand); }
        catch (DataIntegrityViolationException exception) { throw conflict("BRAND_ALREADY_EXISTS", "Thương hiệu đã tồn tại"); }
    }

    private Product saveProduct(Product product) {
        try { return productRepository.save(product); }
        catch (DataIntegrityViolationException exception) { throw conflict("PRODUCT_ALREADY_EXISTS", "Slug hoặc SKU sản phẩm đã tồn tại"); }
    }

    private JsonNode specifications(JsonNode value) {
        return value == null ? JsonNodeFactory.instance.objectNode() : value;
    }

    private void validateProduct(AdminDtos.ProductRequest request) {
        if (request.salePrice() != null && request.salePrice().compareTo(request.basePrice()) > 0) {
            throw validation("Giá khuyến mãi không được lớn hơn giá gốc");
        }
        if (request.specifications() != null && !request.specifications().isObject()) {
            throw validation("Thông số sản phẩm phải là JSON object");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ApiException validation(String message) { return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message); }
    private ApiException conflict(String code, String message) { return new ApiException(HttpStatus.CONFLICT, code, message); }
    private ApiException notFound(String code, String message) { return new ApiException(HttpStatus.NOT_FOUND, code, message); }
}
