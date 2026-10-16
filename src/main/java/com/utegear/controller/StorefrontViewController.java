package com.utegear.controller;

import com.utegear.dto.catalog.ProductDtos;
import com.utegear.dto.order.CustomerOrderDtos;
import com.utegear.exception.ApiException;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.StoreService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StorefrontViewController {

    private static final Map<String, String> CATEGORY_SLUG_ALIASES = Map.of(
            "keyboard", "ban-phim",
            "mouse", "chuot",
            "headset", "tai-nghe",
            "connection-cables", "phu-kien");

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
        ProductDtos.ProductPage onSaleProducts = productCatalogService.search(
                null, null, null, null, null, true, 0, 8, "createdAt,desc");
        model.addAttribute("categories", categoryService.active());
        model.addAttribute("featuredProducts", products.content());
        model.addAttribute("onSaleProducts", onSaleProducts == null ? List.of() : onSaleProducts.content());
        model.addAttribute("bestSellers", List.of());
        model.addAttribute("stores", storeService.findPublic(true, null));
        return "home";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Model model) {
        sort = normalizeSort(sort);
        ProductDtos.ProductPage products = onSale == null
                ? productCatalogService.search(q, categoryId, brandId, minPrice, maxPrice, page, size, sort)
                : productCatalogService.search(q, categoryId, brandId, minPrice, maxPrice, onSale, page, size, sort);
        model.addAttribute("products", products);
        addCatalogFilters(model, q, categoryId, brandId, minPrice, maxPrice, onSale, sort);
        return "products";
    }

    String products(
            String q,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort,
            Model model) {
        model.addAttribute("products", productCatalogService.search(
                q, categoryId, brandId, minPrice, maxPrice, page, size, sort));
        addCatalogFilters(model, q, categoryId, brandId, minPrice, maxPrice, null, sort);
        return "products";
    }

    @GetMapping("/category/{slug}")
    public String category(
            @PathVariable String slug,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Model model) {
        sort = normalizeSort(sort);
        slug = CATEGORY_SLUG_ALIASES.getOrDefault(slug, slug);
        com.utegear.dto.catalog.CategorySummary category;
        try {
            category = categoryService.activeBySlug(slug);
        } catch (ApiException exception) {
            String fallbackQuery = q == null || q.isBlank() ? slug.replace('-', ' ') : q;
            model.addAttribute("products", productCatalogService.search(
                    fallbackQuery, null, null, minPrice, maxPrice, onSale, page, size, sort));
            addCatalogFilters(model, fallbackQuery, null, null, minPrice, maxPrice, onSale, sort);
            return "products";
        }
        model.addAttribute("products", productCatalogService.search(
                q, category.id(), null, minPrice, maxPrice, onSale, page, size, sort));
        addCatalogFilters(model, q, category.id(), null, minPrice, maxPrice, onSale, sort);
        model.addAttribute("currentCategory", category);
        model.addAttribute("categoryBaseUrl", "/category/" + category.slug());
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

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", new CustomerOrderDtos.OrderPage(
                List.of(), 0, 20, 0, 0, true, true, true));
        return "orders/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId) {
        return "redirect:/orders?focus=" + orderId;
    }

    private void addCatalogFilters(
            Model model,
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onSale,
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
        model.addAttribute("selectedOnSale", onSale);
        model.addAttribute("selectedSub", null);
        model.addAttribute("currentCategory", null);
        model.addAttribute("categoryBaseUrl", null);
        model.addAttribute("isCableCategory", false);
        model.addAttribute("isGamepadCategory", false);
        model.addAttribute("cableSubcategories", List.of());
        model.addAttribute("sort", sort);
    }

    private String normalizeSort(String sort) {
        return switch (sort) {
            case "price_asc" -> "effectivePrice,asc";
            case "price_desc" -> "effectivePrice,desc";
            case "latest" -> "createdAt,desc";
            default -> sort;
        };
    }
}
