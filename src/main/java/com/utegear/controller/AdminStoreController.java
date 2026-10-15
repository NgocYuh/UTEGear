package com.utegear.controller;

import com.utegear.dto.store.StoreActiveRequest;
import com.utegear.dto.store.StoreDetail;
import com.utegear.dto.store.StorePage;
import com.utegear.dto.store.StoreUpsertRequest;
import com.utegear.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoreController {

    private final StoreService service;

    public AdminStoreController(StoreService service) {
        this.service = service;
    }

    @GetMapping
    public StorePage findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.findAll(active, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreDetail create(@Valid @RequestBody StoreUpsertRequest request) {
        return service.create(request);
    }

    @GetMapping("/{storeId}")
    public StoreDetail find(@PathVariable Long storeId) {
        return service.findAdminDetail(storeId);
    }

    @PutMapping("/{storeId}")
    public StoreDetail update(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpsertRequest request) {
        return service.update(storeId, request);
    }

    @PatchMapping("/{storeId}/active")
    public StoreDetail changeActive(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreActiveRequest request) {
        return service.changeActive(storeId, request);
    }
}
