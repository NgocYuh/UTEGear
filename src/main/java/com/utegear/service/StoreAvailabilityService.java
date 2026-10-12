package com.utegear.service;

import com.utegear.dto.inventory.AvailabilityDtos;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreAvailabilityService {

    private final StoreRepository storeRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final StoreInventoryRepository inventoryRepository;

    public StoreAvailabilityService(
            StoreRepository storeRepository,
            ProductVariantRepository variantRepository,
            ProductRepository productRepository,
            StoreInventoryRepository inventoryRepository) {
        this.storeRepository = storeRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityDtos.VariantAvailability variantAvailability(Long storeId, Long variantId) {
        Store store = storeRepository.findById(storeId).orElseThrow(this::storeNotFound);
        if (!store.isActive()) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "STORE_INACTIVE", "Cửa hàng đang ngừng hoạt động");
        }
        ProductVariant variant = variantRepository.findById(variantId)
                .filter(ProductVariant::isActive)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm"));
        StoreInventory inventory = inventoryRepository.findByStoreIdAndVariantId(storeId, variantId)
                .orElse(null);
        return AvailabilityDtos.VariantAvailability.from(store, variant, inventory);
    }

    @Transactional(readOnly = true)
    public AvailabilityDtos.ProductAvailability productAvailability(Long productId, String province) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
        String normalizedProvince = normalize(province);
        List<StoreInventory> inventories = inventoryRepository.findProductAvailability(
                productId, normalizedProvince);
        Map<Long, List<StoreInventory>> inventoryByVariant = inventories.stream()
                .collect(Collectors.groupingBy(inventory -> inventory.getVariant().getId()));

        List<AvailabilityDtos.VariantStock> variants = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .map(variant -> variantStock(variant, inventoryByVariant.getOrDefault(variant.getId(), List.of())))
                .toList();
        long totalStock = variants.stream().mapToLong(AvailabilityDtos.VariantStock::totalStock).sum();
        return new AvailabilityDtos.ProductAvailability(productId, totalStock, variants);
    }

    private AvailabilityDtos.VariantStock variantStock(
            ProductVariant variant,
            List<StoreInventory> inventories) {
        List<AvailabilityDtos.AvailableStore> availableStores = inventories.stream()
                .filter(inventory -> inventory.getQuantity() > 0)
                .map(AvailabilityDtos.AvailableStore::from)
                .toList();
        long totalStock = inventories.stream().mapToLong(StoreInventory::getQuantity).sum();
        return new AvailabilityDtos.VariantStock(
                variant.getId(), variant.getSku(), variant.getOptionValues(), variant.isDefault(),
                totalStock, availableStores);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ApiException storeNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "Không tìm thấy cửa hàng");
    }
}
