package com.utegear.service;

import com.utegear.dto.wishlist.WishlistItemResponse;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import com.utegear.entity.WishlistItem;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.WishlistItemRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final StoreInventoryRepository inventoryRepository;

    public WishlistService(
            WishlistItemRepository wishlistRepository,
            ProductRepository productRepository,
            StoreInventoryRepository inventoryRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(User user) {
        requirePersistedUser(user);
        List<WishlistItem> items = wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        Map<Long, Long> stockByVariant = stockFor(items.stream().map(WishlistItem::getProduct).toList());
        return items.stream().map(item -> WishlistItemResponse.from(item, stockByVariant)).toList();
    }

    @Transactional
    public WishlistItemResponse add(User user, Long productId) {
        requirePersistedUser(user);
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> create(user, productId));
        return WishlistItemResponse.from(item, stockFor(List.of(item.getProduct())));
    }

    @Transactional
    public void remove(User user, Long productId) {
        requirePersistedUser(user);
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(this::itemNotFound);
        wishlistRepository.delete(item);
    }

    private WishlistItem create(User user, Long productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));
        return wishlistRepository.save(new WishlistItem(user, product));
    }

    private Map<Long, Long> stockFor(List<Product> products) {
        List<Long> variantIds = products.stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (variantIds.isEmpty()) {
            return Map.of();
        }
        return inventoryRepository.sumStockByVariantIds(variantIds).stream()
                .collect(Collectors.toMap(
                        StoreInventoryRepository.VariantStock::getVariantId,
                        StoreInventoryRepository.VariantStock::getQuantity));
    }

    private void requirePersistedUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Người dùng đã xác thực là bắt buộc");
        }
    }

    private ApiException itemNotFound() {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "WISHLIST_ITEM_NOT_FOUND",
                "Không tìm thấy sản phẩm trong danh sách yêu thích");
    }
}
