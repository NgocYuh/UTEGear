package com.utegear.dto.store;

import com.utegear.entity.Store;
import java.math.BigDecimal;
import java.time.LocalTime;

public record StoreDetail(
        Long id,
        String name,
        String address,
        String province,
        String district,
        String phone,
        String email,
        String imageUrl,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active,
        String description,
        BigDecimal latitude,
        BigDecimal longitude) {

    public static StoreDetail from(Store store) {
        return new StoreDetail(
                store.getId(), store.getName(), store.getAddress(), store.getProvince(),
                store.getDistrict(), store.getPhone(), store.getEmail(), store.getImageUrl(),
                store.getOpeningTime(), store.getClosingTime(), store.isActive(),
                store.getDescription(), store.getLatitude(), store.getLongitude());
    }
}
