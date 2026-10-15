package com.utegear.controller;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.ProductStatus;
import com.utegear.service.AdminCatalogService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/admin")
public class AdminCatalogController {
    private final AdminCatalogService service;

    public AdminCatalogController(AdminCatalogService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<AdminDtos.CategoryView> categories() { return service.categories(); }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.CategoryView createCategory(@Valid @RequestBody AdminDtos.CategoryRequest request) {
        return service.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public AdminDtos.CategoryView updateCategory(
            @PathVariable Long id, @Valid @RequestBody AdminDtos.CategoryRequest request) {
        return service.updateCategory(id, request);
    }

    @GetMapping("/brands")
    public List<AdminDtos.BrandView> brands() { return service.brands(); }

    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.BrandView createBrand(@Valid @RequestBody AdminDtos.BrandRequest request) {
        return service.createBrand(request);
    }

    @PutMapping("/brands/{id}")
    public AdminDtos.BrandView updateBrand(
            @PathVariable Long id, @Valid @RequestBody AdminDtos.BrandRequest request) {
        return service.updateBrand(id, request);
    }

    @GetMapping("/products")
    public AdminDtos.ProductPage products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.products(q, status, page, size);
    }

    @GetMapping("/products/{id}")
    public AdminDtos.ProductView product(@PathVariable Long id) { return service.product(id); }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.ProductView createProduct(@Valid @RequestBody AdminDtos.ProductRequest request) {
        return service.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public AdminDtos.ProductView updateProduct(
            @PathVariable Long id, @Valid @RequestBody AdminDtos.ProductRequest request) {
        return service.updateProduct(id, request);
    }

    @PatchMapping("/products/{id}/status")
    public AdminDtos.ProductView changeProductStatus(
            @PathVariable Long id, @Valid @RequestBody AdminDtos.ProductStatusRequest request) {
        return service.changeProductStatus(id, request.status());
    }
}
