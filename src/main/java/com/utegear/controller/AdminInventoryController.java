package com.utegear.controller;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.dto.inventory.InventoryResult;
import com.utegear.dto.inventory.InventoryTransferResult;
import com.utegear.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminInventoryController {
    private final InventoryService service;

    public AdminInventoryController(InventoryService service) { this.service = service; }

    @GetMapping("/inventory")
    public List<AdminDtos.InventoryView> findAll(@RequestParam(required = false) Long storeId) {
        return service.findAll(storeId);
    }

    @PutMapping("/stores/{storeId}/inventory/{variantId}")
    public InventoryResult setQuantity(
            @PathVariable Long storeId,
            @PathVariable Long variantId,
            @Valid @RequestBody AdminDtos.InventoryQuantityRequest request) {
        return service.setQuantity(storeId, variantId, request.quantity());
    }

    @PostMapping("/inventory/transfers")
    public InventoryTransferResult transfer(@Valid @RequestBody AdminDtos.InventoryTransferRequest request) {
        return service.transfer(request.sourceStoreId(), request.destinationStoreId(),
                request.variantId(), request.quantity());
    }
}
