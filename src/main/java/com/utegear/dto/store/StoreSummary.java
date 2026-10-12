package com.utegear.dto.store;

import com.utegear.entity.Store;
import java.time.LocalTime;

public record StoreSummary(
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
        boolean active) {

    public static StoreSummary from(Store store) {
        return new StoreSummary(
                store.getId(), store.getName(), store.getAddress(), store.getProvince(),
                store.getDistrict(), store.getPhone(), store.getEmail(), store.getImageUrl(),
                store.getOpeningTime(), store.getClosingTime(), store.isActive());
    }
}
