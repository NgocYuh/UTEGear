package com.utegear.dto.store;

import org.springframework.data.domain.Page;

import java.util.List;

public record StorePage(
        List<StoreDetail> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean empty) {

    public static StorePage from(Page<StoreDetail> source) {
        return new StorePage(
                source.getContent(), source.getNumber(), source.getSize(), source.getTotalElements(),
                source.getTotalPages(), source.isEmpty());
    }
}
