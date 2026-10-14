package com.utegear.controller;

import com.utegear.dto.catalog.ProductDtos;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.StoreService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StorefrontViewController {

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductCatalogService productCatalogService;
    private final StoreService storeService;

    public StorefrontViewController(
            CategoryService categoryService,
            BrandService brandService,
            ProductCatalogService productCatalogService,
            StoreService storeService) {
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productCatalogService = productCatalogService;
        this.storeService = storeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        ProductDtos.ProductPage products = productCatalogService.search(
                null, null, null, null, null, 0, 8, "createdAt,desc");
        model.addAttribute("categories", categoryService.active());
        model.addAttribute("featuredProducts", products.content());
        model.addAttribute("onSaleProducts", List.of());
        model.addAttribute("bestSellers", List.of());
        return "home";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Model model) {
        model.addAttribute("products", productCatalogService.search(
                q, categoryId, brandId, minPrice, maxPrice, page, size, sort));
        addCatalogFilters(model, q, categoryId, brandId, minPrice, maxPrice, sort);
        return "products";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q, Model model) {
        ProductDtos.ProductPage products = productCatalogService.search(
                q, null, null, null, null, 0, 20, "createdAt,desc");
        model.addAttribute("keyword", q);
        model.addAttribute("searchResults", products.content());
        return "search";
    }

    @GetMapping("/products/{productId}")
    public String product(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productCatalogService.detail(productId));
        return "product-detail";
    }

    @GetMapping("/stores")
    public String stores(Model model) {
        model.addAttribute("stores", storeService.findPublic(true, null));
        return "stores/stores";
    }

    @GetMapping("/stores/{storeId}")
    public String store(@PathVariable Long storeId, Model model) {
        model.addAttribute("store", storeService.findPublicDetail(storeId));
        model.addAttribute("products", List.of());
        return "stores/store-detail";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("cartItems", List.of());
        model.addAttribute("totalQuantity", 0);
        model.addAttribute("subtotal", BigDecimal.ZERO);
        return "cart";
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        model.addAttribute("wishlistItems", List.of());
        return "wishlist";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("cartItems", List.of());
        model.addAttribute("stores", storeService.findPublic(true, null));
        model.addAttribute("subtotal", BigDecimal.ZERO);
        return "checkout";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile/profile";
    }

    private void addCatalogFilters(
            Model model,
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("allCategories", categoryService.active());
        model.addAttribute("allBrands", brandService.active());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedMinRating", null);
        model.addAttribute("selectedInStock", null);
        model.addAttribute("selectedOnSale", null);
        model.addAttribute("selectedSub", null);
        model.addAttribute("currentCategory", null);
        model.addAttribute("categoryBaseUrl", null);
        model.addAttribute("isCableCategory", false);
        model.addAttribute("isGamepadCategory", false);
        model.addAttribute("cableSubcategories", List.of());
        model.addAttribute("sort", sort);
    }
}
