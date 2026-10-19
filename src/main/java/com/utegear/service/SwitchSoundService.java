package com.utegear.service;

import com.utegear.dto.catalog.SwitchSoundDtos;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SwitchSoundService {

    private static final String DEFAULT_DISCLAIMER =
            "Âm thanh thực tế phụ thuộc vào chất liệu vỏ phím, tấm plate, keycap và thiết bị nghe của bạn.";

    private static final List<SwitchSoundDtos.SwitchProfile> BASE_PROFILES = List.of(
            new SwitchSoundDtos.SwitchProfile(
                    "RED_LINEAR",
                    "Red Switch (Linear)",
                    "Linear",
                    45,
                    2.0,
                    4.0,
                    "Êm",
                    "Trơn mượt, không khấc, âm trầm nhẹ (thocky), phù hợp chơi game tốc độ và văn phòng.",
                    "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
                    "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/red-linear.mp3",
                    "utegear/switches/red-linear",
                    "#E53E3E",
                    new SwitchSoundDtos.SynthProfile(180, 0, 0.0, 95, 1.2),
                    List.of()),
            new SwitchSoundDtos.SwitchProfile(
                    "BLUE_CLICKY",
                    "Blue Switch (Clicky)",
                    "Clicky",
                    50,
                    2.2,
                    4.0,
                    "Đanh / Tiếng Click rõ",
                    "Khấc bấm rõ rệt và tiếng click đanh đặc trưng, gõ nảy tay.",
                    "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
                    "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/blue-clicky.mp3",
                    "utegear/switches/blue-clicky",
                    "#3182CE",
                    new SwitchSoundDtos.SynthProfile(220, 3200, 0.7, 110, 0.8),
                    List.of()),
            new SwitchSoundDtos.SwitchProfile(
                    "BROWN_TACTILE",
                    "Brown Switch (Tactile)",
                    "Tactile",
                    45,
                    2.0,
                    4.0,
                    "Vừa phải",
                    "Có khấc phản hồi nhẹ khi chạm điểm kích hoạt, âm đầm ấm cân bằng giữa làm việc và chơi game.",
                    "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
                    "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/brown-tactile.mp3",
                    "utegear/switches/brown-tactile",
                    "#975A16",
                    new SwitchSoundDtos.SynthProfile(195, 1800, 0.25, 100, 1.0),
                    List.of()),
            new SwitchSoundDtos.SwitchProfile(
                    "SILVER_SPEED",
                    "Silver Switch (Speed Linear)",
                    "Linear",
                    40,
                    1.2,
                    3.4,
                    "Êm / Tốc độ",
                    "Hành trình kích hoạt cực ngắn 1.2mm, phản hồi siêu nhạy, âm thanh gõ thanh thoát dồn dập.",
                    "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
                    "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/silver-speed.mp3",
                    "utegear/switches/silver-speed",
                    "#CBD5E0",
                    new SwitchSoundDtos.SynthProfile(230, 0, 0.0, 75, 0.9),
                    List.of()),
            new SwitchSoundDtos.SwitchProfile(
                    "SILENT_RED",
                    "Silent Red Switch (Silent Linear)",
                    "Silent Linear",
                    45,
                    1.9,
                    3.7,
                    "Rất êm / Giảm chấn",
                    "Tích hợp đệm cao su giảm chấn ở cả 2 chiều lên xuống, âm thanh cực kỳ yên tĩnh cho đêm khuya và văn phòng.",
                    "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
                    "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/silent-red.mp3",
                    "utegear/switches/silent-red",
                    "#FEB2B2",
                    new SwitchSoundDtos.SynthProfile(140, 0, 0.0, 60, 0.4),
                    List.of()));

    private final ProductRepository productRepository;

    public SwitchSoundService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<SwitchSoundDtos.SwitchProfile> findAllProfiles() {
        return BASE_PROFILES;
    }

    @Transactional(readOnly = true)
    public SwitchSoundDtos.ProductSwitchSoundResponse forProduct(Long productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));

        String categorySlug = product.getCategory() != null ? product.getCategory().getSlug() : "";
        String productName = product.getName();
        boolean isKeyboard = isKeyboardProduct(categorySlug, productName);

        if (!isKeyboard) {
            return new SwitchSoundDtos.ProductSwitchSoundResponse(
                    product.getId(),
                    productName,
                    categorySlug,
                    false,
                    null,
                    "Sản phẩm này không hỗ trợ tính năng thử nghiệm switch bàn phím.",
                    List.of());
        }

        Long defaultVariantId = product.getVariants().stream()
                .filter(ProductVariant::isDefault)
                .map(ProductVariant::getId)
                .findFirst()
                .orElse(null);

        List<SwitchSoundDtos.SwitchProfile> resolvedProfiles = new ArrayList<>();
        String detectedDefaultCode = null;

        for (SwitchSoundDtos.SwitchProfile base : BASE_PROFILES) {
            List<Long> matchedVariantIds = findMatchedVariants(product.getVariants(), base.code());
            if (defaultVariantId != null && matchedVariantIds.contains(defaultVariantId)) {
                detectedDefaultCode = base.code();
            }
            resolvedProfiles.add(base.withMatchedVariantIds(matchedVariantIds));
        }

        if (detectedDefaultCode == null) {
            detectedDefaultCode = resolvedProfiles.stream()
                    .filter(p -> !p.matchedVariantIds().isEmpty())
                    .map(SwitchSoundDtos.SwitchProfile::code)
                    .findFirst()
                    .orElse("RED_LINEAR");
        }

        SwitchSoundDtos.KeyboardAppearance appearance = detectAppearance(product);

        return new SwitchSoundDtos.ProductSwitchSoundResponse(
                product.getId(),
                productName,
                categorySlug,
                true,
                detectedDefaultCode,
                DEFAULT_DISCLAIMER,
                resolvedProfiles,
                appearance.layoutType(),
                detectThemeColor(product),
                appearance);
    }

    private boolean isKeyboardProduct(String categorySlug, String productName) {
        if ("ban-phim".equalsIgnoreCase(categorySlug) || "keyboard".equalsIgnoreCase(categorySlug)) {
            return true;
        }
        String normalizedName = productName.toLowerCase(Locale.ROOT);
        return normalizedName.contains("bàn phím") || normalizedName.contains("keyboard");
    }

    private List<Long> findMatchedVariants(List<ProductVariant> variants, String switchCode) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }
        return variants.stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> matchVariant(variant, switchCode))
                .map(ProductVariant::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean matchVariant(ProductVariant variant, String switchCode) {
        StringBuilder combined = new StringBuilder();
        if (variant.getSku() != null) {
            combined.append(variant.getSku()).append(' ');
        }
        Map<String, String> optionValues = variant.getOptionValues();
        if (optionValues != null) {
            for (String val : optionValues.values()) {
                if (val != null) {
                    combined.append(val).append(' ');
                }
            }
        }
        String text = combined.toString().toLowerCase(Locale.ROOT);

        return switch (switchCode) {
            case "RED_LINEAR" -> (text.contains("red") || text.contains("đỏ")) && !text.contains("silent");
            case "BLUE_CLICKY" -> text.contains("blue") || text.contains("xanh");
            case "BROWN_TACTILE" -> text.contains("brown") || text.contains("nâu");
            case "SILVER_SPEED" -> text.contains("silver") || text.contains("bạc") || text.contains("speed");
            case "SILENT_RED" -> text.contains("silent");
            default -> false;
        };
    }

    private SwitchSoundDtos.KeyboardAppearance detectAppearance(Product product) {
        String specs = product.getSpecifications() != null ? product.getSpecifications().toString() : "";
        String name = product.getName() != null ? product.getName().toLowerCase(Locale.ROOT) : "";
        String combined = (name + " " + specs).toLowerCase(Locale.ROOT);

        String layoutType = "TKL";
        String layoutDisplayName = "Layout: 80% TKL (87 phím)";
        boolean hasKnob = false;

        // Specific brand & series detection
        if (combined.contains("f75") || combined.contains("75%") || combined.contains("81 phím") || combined.contains("82 phím") || combined.contains("84 phím")) {
            layoutType = "LAYOUT_75";
            layoutDisplayName = "Layout: 75% Exploded (81 phím)";
            hasKnob = true;
        } else if (combined.contains("108 phím") || combined.contains("104 phím") || combined.contains("100%")
                || combined.contains("5108") || combined.contains("f108") || combined.contains("s102") || combined.contains("fullsize")
                || combined.contains("k70") || combined.contains("g512") || combined.contains("blackwidow v4")) {
            layoutType = "FULLSIZE";
            layoutDisplayName = "Layout: 100% Fullsize (108 phím)";
            hasKnob = combined.contains("núm xoay") || combined.contains("knob");
        } else if (combined.contains("65%") || combined.contains("67 phím") || combined.contains("68 phím")
                || combined.contains("60%") || combined.contains("mini 65") || combined.contains("blackwidow v3 mini") || combined.contains("falchion")) {
            layoutType = "COMPACT_65";
            layoutDisplayName = "Layout: 65% Mini (68 phím)";
            hasKnob = combined.contains("núm xoay") || combined.contains("knob");
        } else {
            layoutType = "TKL";
            layoutDisplayName = "Layout: 80% TKL (87 phím)";
            hasKnob = combined.contains("núm xoay") || combined.contains("knob");
        }

        String caseColor = "#18181b";
        String alphaColor = "#27272a";
        String alphaTextColor = "#f4f4f5";
        String modColor = "#18181b";
        String modTextColor = "#a1a1aa";
        String accentColor = "#e11d48";
        String accentTextColor = "#ffffff";

        if (combined.contains("logitech") || combined.contains("g515") || combined.contains("g pro") || combined.contains("g512") || combined.contains("g915") || combined.contains("g413") || combined.contains("g213")) {
            // Logitech Gaming (Anodized Dark Metal Case & Lilac Purple Arrow Cluster)
            if (combined.contains("tkl") && !combined.contains("96") && !combined.contains("numpad")) {
                layoutType = "TKL";
                layoutDisplayName = "Logitech G TKL Lightspeed";
            } else {
                layoutType = "LAYOUT_96";
                layoutDisplayName = "Logitech G Compact 96% (Numpad + Dual Knobs)";
            }
            hasKnob = true;
            caseColor = "#121216";
            alphaColor = "#18181c";
            alphaTextColor = "#ffffff";
            modColor = "#18181c";
            modTextColor = "#ffffff";
            accentColor = "#8b5cf6";
            accentTextColor = "#ffffff";
        } else if (combined.contains("cam") || (combined.contains("xanh") && combined.contains("f75")) || combined.contains("reaper") || combined.contains("f75") || combined.contains("aula")) {
            // AULA F75 Navy / Black / Orange Reaper Axis (100% authentic to product image)
            caseColor = "#121316";
            alphaColor = "#1e222d";
            alphaTextColor = "#fbbf24";
            modColor = "#161922";
            modTextColor = "#fbbf24";
            accentColor = "#f59e0b";
            accentTextColor = "#18181b";
        } else if (combined.contains("hello kitty") || (combined.contains("5108") && (combined.contains("kitty") || combined.contains("hồng") || combined.contains("pink")))) {
            // AKKO 5108 V5 Hello Kitty Edition (White & Soft Sakura Pink with Rose Red legends & pink key borders)
            caseColor = "#ffe4ef";
            alphaColor = "#ffffff";
            alphaTextColor = "#e11d48";
            modColor = "#fff0f6";
            modTextColor = "#db2777";
            accentColor = "#ffd6e7";
            accentTextColor = "#be185d";
        } else if (combined.contains("hồng") || combined.contains("pink")) {
            // General Pink Pastel Keyboards
            caseColor = "#fce7f3";
            alphaColor = "#ffffff";
            alphaTextColor = "#db2777";
            modColor = "#fbcfe8";
            modTextColor = "#be185d";
            accentColor = "#f472b6";
            accentTextColor = "#ffffff";
        } else if (combined.contains("xanh lá") || combined.contains("green") || combined.contains("legend of hei") || combined.contains("hei")) {
            // Legend of Hei / Forest Green
            caseColor = "#064e3b";
            alphaColor = "#f0fdf4";
            alphaTextColor = "#064e3b";
            modColor = "#065f46";
            modTextColor = "#a7f3d0";
            accentColor = "#10b981";
            accentTextColor = "#ffffff";
        } else if (combined.contains("trắng") || combined.contains("white") || combined.contains("pro x") || combined.contains("tkl white")) {
            // Pure White / Ice Blue Accent (Logitech PRO X, etc.)
            caseColor = "#f8fafc";
            alphaColor = "#ffffff";
            alphaTextColor = "#0f172a";
            modColor = "#f1f5f9";
            modTextColor = "#334155";
            accentColor = "#38bdf8";
            accentTextColor = "#ffffff";
        } else if (combined.contains("tím") || combined.contains("purple") || combined.contains("tokyo")) {
            // Tokyo Neon / Purple Cyberpunk
            caseColor = "#1e1b4b";
            alphaColor = "#ffffff";
            alphaTextColor = "#1e1b4b";
            modColor = "#3b82f6";
            modTextColor = "#ffffff";
            accentColor = "#7c3aed";
            accentTextColor = "#ffffff";
        } else if (combined.contains("midnight") || combined.contains("navy") || combined.contains("3087") || combined.contains("horizon")) {
            // Akko 3087 Midnight / Deep Navy Blue
            caseColor = "#0a1128";
            alphaColor = "#1c2e4a";
            alphaTextColor = "#f8fafc";
            modColor = "#0f1b29";
            modTextColor = "#94a3b8";
            accentColor = "#d97706";
            accentTextColor = "#ffffff";
        } else if (combined.contains("vàng") || combined.contains("yellow") || combined.contains("monet") || combined.contains("doraemon")) {
            // Yellow / Cyan Accent
            caseColor = "#1c1917";
            alphaColor = "#fef08a";
            alphaTextColor = "#713f12";
            modColor = "#292524";
            modTextColor = "#eab308";
            accentColor = "#06b6d4";
            accentTextColor = "#ffffff";
        } else if (combined.contains("xám") || combined.contains("gray") || combined.contains("grey") || combined.contains("retro")) {
            // Retro Classic Gray / Red Accent
            caseColor = "#27272a";
            alphaColor = "#e4e4e7";
            alphaTextColor = "#18181b";
            modColor = "#71717a";
            modTextColor = "#ffffff";
            accentColor = "#ef4444";
            accentTextColor = "#ffffff";
        } else {
            // Stealth Gaming Black with Red / Crimson Accent (Corsair, Razer, DareU)
            caseColor = "#121214";
            alphaColor = "#1e1e24";
            alphaTextColor = "#f4f4f5";
            modColor = "#141416";
            modTextColor = "#a1a1aa";
            accentColor = "#e11d48";
            accentTextColor = "#ffffff";
        }

        return new SwitchSoundDtos.KeyboardAppearance(
                layoutType,
                hasKnob,
                caseColor,
                alphaColor,
                alphaTextColor,
                modColor,
                modTextColor,
                accentColor,
                accentTextColor,
                layoutDisplayName);
    }

    private String detectThemeColor(Product product) {
        String specs = product.getSpecifications() != null ? product.getSpecifications().toString() : "";
        String name = product.getName() != null ? product.getName().toLowerCase(Locale.ROOT) : "";
        String combined = (name + " " + specs).toLowerCase(Locale.ROOT);

        if (combined.contains("trắng") || combined.contains("white")) {
            return "WHITE";
        }
        if (combined.contains("hồng") || combined.contains("pink") || combined.contains("hello kitty")) {
            return "PINK";
        }
        if (combined.contains("xanh lá") || combined.contains("green") || combined.contains("legend of hei")) {
            return "GREEN";
        }
        if (combined.contains("midnight") || combined.contains("navy") || combined.contains("3087")) {
            return "MIDNIGHT";
        }
        return "BLACK";
    }
}
