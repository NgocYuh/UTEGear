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
        validateFilters(minPrice, maxPrice, page, size);
        String[] sortParts = parseSort(sort);
        Specification<Product> specification = specification(
                query, categoryId, brandId, minPrice, maxPrice, sortParts);
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

    private Specification<Product> specification(
            String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            String[] sortParts) {
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

    private void validateFilters(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || minPrice != null && minPrice.signum() < 0
                || maxPrice != null && (maxPrice.signum() < 0
                || minPrice != null && maxPrice.compareTo(minPrice) < 0)) {
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
