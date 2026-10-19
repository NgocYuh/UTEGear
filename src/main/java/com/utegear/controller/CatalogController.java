package com.utegear.controller;

import com.utegear.dto.catalog.BrandSummary;
import com.utegear.dto.catalog.CategorySummary;
import com.utegear.dto.catalog.ProductDtos;
import com.utegear.dto.catalog.SwitchSoundDtos;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.SwitchSoundService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CategoryService categories;
    private final BrandService brands;
    private final ProductCatalogService products;
    private final SwitchSoundService switchSounds;

    public CatalogController(
            CategoryService categories,
            BrandService brands,
            ProductCatalogService products,
            SwitchSoundService switchSounds) {
        this.categories = categories;
        this.brands = brands;
        this.products = products;
        this.switchSounds = switchSounds;
    }

    @GetMapping("/categories")
    public List<CategorySummary> categories() {
        return categories.active();
    }

    @GetMapping("/brands")
    public List<BrandSummary> brands() {
        return brands.active();
    }

    @GetMapping("/products")
    public ProductDtos.ProductPage products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return onSale == null
                ? products.search(q, categoryId, brandId, minPrice, maxPrice, page, size, sort)
                : products.search(q, categoryId, brandId, minPrice, maxPrice, onSale, page, size, sort);
    }

    @GetMapping("/products/{id}")
    public ProductDtos.Detail product(@PathVariable Long id) {
        return products.detail(id);
    }

    @GetMapping("/products/switch-sounds")
    public List<SwitchSoundDtos.SwitchProfile> switchSounds() {
        return switchSounds.findAllProfiles();
    }

    @GetMapping("/products/{id}/switch-sounds")
    public SwitchSoundDtos.ProductSwitchSoundResponse productSwitchSounds(@PathVariable Long id) {
        return switchSounds.forProduct(id);
    }
}
