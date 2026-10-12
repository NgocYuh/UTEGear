package com.utegear.controller;

import com.utegear.dto.inventory.AvailabilityDtos;
import com.utegear.dto.store.StoreDetail;
import com.utegear.dto.store.StoreSummary;
import com.utegear.service.StoreAvailabilityService;
import com.utegear.service.StoreService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;
    private final StoreAvailabilityService availabilityService;

    public StoreController(StoreService storeService, StoreAvailabilityService availabilityService) {
        this.storeService = storeService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/stores")
    public List<StoreSummary> stores(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String province) {
        return storeService.findPublic(active, province);
    }

    @GetMapping("/stores/{storeId}")
    public StoreDetail store(@PathVariable Long storeId) {
        return storeService.findPublicDetail(storeId);
    }

    @GetMapping("/stores/{storeId}/availability")
    public AvailabilityDtos.VariantAvailability variantAvailability(
            @PathVariable Long storeId,
            @RequestParam Long variantId) {
        return availabilityService.variantAvailability(storeId, variantId);
    }

    @GetMapping("/products/{productId}/availability")
    public AvailabilityDtos.ProductAvailability productAvailability(
            @PathVariable Long productId,
            @RequestParam(required = false) String province) {
        return availabilityService.productAvailability(productId, province);
    }
}
