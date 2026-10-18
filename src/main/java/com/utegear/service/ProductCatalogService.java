package com.utegear.service;

import com.utegear.dto.catalog.ProductDtos;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCatalogService {
    private static final Set<String> SORTS = Set.of("name", "effectivePrice", "createdAt");
    private final ProductRepository productRepository;
    private final StoreInventoryRepository inventoryRepository;

    public ProductCatalogService(
            ProductRepository productRepository,
            StoreInventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public ProductDtos.ProductPage search(
            String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sort) {
        return search(query, categoryId, brandId, minPrice, maxPrice, null, page, size, sort);
    }

    @Transactional(readOnly = true)
    public ProductDtos.ProductPage search(
            String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean onSale, int page, int size, String sort) {
        validateFilters(query, minPrice, maxPrice, page, size);
        String[] sortParts = parseSort(sort);
        Specification<Product> specification = specification(
                query, categoryId, brandId, minPrice, maxPrice, onSale, sortParts);
        Page<Product> products = productRepository.findAll(specification, PageRequest.of(page, size));
        return ProductDtos.ProductPage.from(products, stockFor(products.getContent()));
    }

    @Transactional(readOnly = true)
    public ProductDtos.Detail detail(Long id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
        return ProductDtos.Detail.from(product, stockFor(List.of(product)));
    }

    @Transactional(readOnly = true)
    public List<ProductDtos.Summary> inStockAtStore(Long storeId) {
        List<Product> products = productRepository.findInStockAtStore(storeId, ProductStatus.ACTIVE);
        if (products.isEmpty()) return List.of();
        productRepository.findWithImagesByIds(products.stream().map(Product::getId).toList());
        List<Long> variantIds = products.stream().flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive).map(ProductVariant::getId).toList();
        Map<Long, Long> stock = inventoryRepository.sumStoreStockByVariantIds(storeId, variantIds).stream()
                .collect(Collectors.toMap(StoreInventoryRepository.VariantStock::getVariantId,
                        StoreInventoryRepository.VariantStock::getQuantity));
        return products.stream().map(product -> ProductDtos.Summary.from(product, stock)).toList();
    }

    private Specification<Product> specification(
            String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean onSale, String[] sortParts) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));
            if (query != null && !query.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + query.trim().toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }
            if (Boolean.TRUE.equals(onSale)) {
                predicates.add(criteriaBuilder.isNotNull(root.get("salePrice")));
                predicates.add(criteriaBuilder.lessThan(root.get("salePrice"), root.get("basePrice")));
            }
            Expression<BigDecimal> price = criteriaBuilder.<BigDecimal>coalesce()
                    .value(root.get("salePrice")).value(root.get("basePrice"));
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(price, minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(price, maxPrice));
            }
            if (criteriaQuery.getResultType() != Long.class) {
                Expression<?> order = sortParts[0].equals("effectivePrice")
                        ? price : root.get(sortParts[0]);
                criteriaQuery.orderBy(sortParts[1].equals("asc")
                        ? criteriaBuilder.asc(order) : criteriaBuilder.desc(order));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, Long> stockFor(Collection<Product> products) {
        List<Long> variantIds = products.stream().flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive).map(ProductVariant::getId).filter(id -> id != null).toList();
        if (variantIds.isEmpty()) {
            return Map.of();
        }
        return inventoryRepository.sumStockByVariantIds(variantIds).stream()
                .collect(Collectors.toMap(StoreInventoryRepository.VariantStock::getVariantId,
                        StoreInventoryRepository.VariantStock::getQuantity));
    }

    private void validateFilters(String query, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        if (page < 0 || size < 1 || size > 100
                || (query != null && query.trim().length() > 200)
                || (minPrice != null && minPrice.signum() < 0)
                || (maxPrice != null && (maxPrice.signum() < 0
                || (minPrice != null && maxPrice.compareTo(minPrice) < 0)))) {
            throw invalidFilter();
        }
    }

    private String[] parseSort(String sort) {
        String[] parts = sort.split(",", -1);
        if (parts.length != 2 || !SORTS.contains(parts[0])
                || !(parts[1].equals("asc") || parts[1].equals("desc"))) {
            throw invalidFilter();
        }
        return parts;
    }

    private ApiException invalidFilter() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FILTER", "Bộ lọc không hợp lệ");
    }
}
