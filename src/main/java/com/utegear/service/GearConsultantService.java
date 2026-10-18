package com.utegear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.utegear.dto.consultant.ConsultantDtos.*;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GearConsultantService {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(Locale.of("vi", "VN"));

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;

    public GearConsultantService(
            ProductRepository productRepository,
            StoreRepository storeRepository,
            StoreInventoryRepository storeInventoryRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.storeInventoryRepository = storeInventoryRepository;
    }

    public List<QuickPrompt> getQuickPrompts() {
        return List.of(
                new QuickPrompt(
                        "combo-fps-danang",
                        "Combo FPS tay nhỏ dưới 1.5tr (Đà Nẵng)",
                        "Tui hay chơi game FPS (CS2/Valorant), tay nhỏ, ngân sách dưới 1.5 triệu, đề xuất combo chuột và phím phù hợp ở chi nhánh Đà Nẵng",
                        "bi-crosshair"),
                new QuickPrompt(
                        "mouse-wireless-moba",
                        "Chuột không dây siêu nhẹ chơi MOBA",
                        "Tư vấn chuột gaming không dây trọng lượng siêu nhẹ, cầm vừa tay để cày LMHT / Dota 2",
                        "bi-mouse"),
                new QuickPrompt(
                        "keyboard-silent-office",
                        "Bàn phím cơ gõ êm văn phòng",
                        "Tìm bàn phím cơ switch êm không ồn, gõ văn phòng và làm việc ban đêm không ảnh hưởng người xung quanh",
                        "bi-keyboard"),
                new QuickPrompt(
                        "headset-fps-budget",
                        "Tai nghe gaming dưới 1 triệu",
                        "Tư vấn tai nghe gaming nghe rõ tiếng bước chân địch trong CS2/Valorant, mic đàm thoại rõ ràng dưới 1 triệu",
                        "bi-headphones"));
    }

    @Transactional(readOnly = true)
    public ChatResponse consult(ChatRequest request) {
        String originalMessage = request.message() == null ? "" : request.message().trim();
        String normalized = removeAccents(originalMessage.toLowerCase(Locale.ROOT));

        // 1. Detect store
        Store store = resolveTargetStore(request.storeId(), normalized);

        // 2. Parse intent: game type, category, hand size, budget
        Intent intent = parseIntent(normalized);

        // 3. Load active products in stock at this store
        List<Product> availableProducts = productRepository.findInStockAtStore(store.getId(), ProductStatus.ACTIVE);
        if (!availableProducts.isEmpty()) {
            List<Long> productIds = availableProducts.stream().map(Product::getId).toList();
            productRepository.findWithImagesByIds(productIds);
        }

        // 4. Calculate exact store inventory per product
        Map<Long, Integer> stockMap = calculateStockMap(store.getId());

        // 5. Score candidates
        List<ScoredProduct> scored = availableProducts.stream()
                .map(product -> scoreProduct(product, intent, stockMap.getOrDefault(product.getId(), 0)))
                .filter(sp -> sp.score > 0)
                .sorted(Comparator.comparingInt(ScoredProduct::score).reversed())
                .toList();

        // 6. Selection: Combo vs Single category
        List<ScoredProduct> selected;
        if (intent.isCombo) {
            selected = pickOptimalCombo(scored, intent.budget);
        } else {
            selected = pickTopProducts(scored, intent.budget, 3);
        }

        // 7. Build recommended product views
        List<RecommendedProduct> productViews = selected.stream()
                .map(sp -> toRecommendedProductView(sp.product, sp.score, sp.reason, stockMap.getOrDefault(sp.product.getId(), 0), store))
                .toList();

        // 8. Calculate total estimate
        BigDecimal totalEstimate = productViews.stream()
                .map(RecommendedProduct::effectivePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 9. Generate explanation reply & follow-up suggestions
        String reply = buildConsultantReply(intent, store, productViews, totalEstimate);
        List<String> nextSuggestions = buildNextSuggestions(intent, store);

        return new ChatResponse(
                reply,
                intent.toDisplaySummary(),
                new StoreInfo(store.getId(), store.getName(), store.getProvince(), store.getDistrict()),
                intent.budget,
                totalEstimate,
                productViews,
                nextSuggestions);
    }

    private Store resolveTargetStore(Long storeId, String normalized) {
        if (storeId != null) {
            return storeRepository.findByIdAndActiveTrue(storeId)
                    .orElseGet(() -> getDefaultStore());
        }
        if (normalized.contains("da nang")) {
            return findStoreByProvinceKeywords("Đà Nẵng", "da nang");
        }
        if (normalized.contains("ha noi")) {
            return findStoreByProvinceKeywords("Hà Nội", "ha noi");
        }
        if (normalized.contains("ho chi minh") || normalized.contains("tp.hcm") || normalized.contains("tphcm")
                || normalized.contains("sai gon") || normalized.contains("quan 1") || normalized.contains("thu duc")) {
            return findStoreByProvinceKeywords("TP.HCM", "ho chi minh", "tp.hcm", "tphcm", "sai gon", "thu duc");
        }
        return getDefaultStore();
    }

    private Store findStoreByProvinceKeywords(String province, String... keywords) {
        List<Store> stores = storeRepository.findByActiveOrderByNameAsc(true);
        for (Store s : stores) {
            String p = s.getProvince() == null ? "" : removeAccents(s.getProvince().toLowerCase(Locale.ROOT));
            String d = s.getDistrict() == null ? "" : removeAccents(s.getDistrict().toLowerCase(Locale.ROOT));
            String name = s.getName() == null ? "" : removeAccents(s.getName().toLowerCase(Locale.ROOT));
            for (String kw : keywords) {
                String normKw = removeAccents(kw.toLowerCase(Locale.ROOT));
                if (p.contains(normKw) || d.contains(normKw) || name.contains(normKw)) {
                    return s;
                }
            }
        }
        return stores.stream().findFirst().orElseThrow(() -> new IllegalStateException("Hệ thống chưa có chi nhánh hoạt động"));
    }

    private Store getDefaultStore() {
        return storeRepository.findByActiveOrderByNameAsc(true).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hệ thống chưa có chi nhánh hoạt động"));
    }

    private Map<Long, Integer> calculateStockMap(Long storeId) {
        List<StoreInventory> inventories = storeInventoryRepository.findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(storeId);
        Map<Long, Integer> map = new HashMap<>();
        for (StoreInventory inv : inventories) {
            if (inv.getVariant() != null && inv.getVariant().getProduct() != null && inv.getVariant().isActive()) {
                Long productId = inv.getVariant().getProduct().getId();
                map.merge(productId, inv.getQuantity(), Integer::sum);
            }
        }
        return map;
    }

    private Intent parseIntent(String normalized) {
        Intent intent = new Intent();

        // Combo vs category
        if (normalized.contains("combo") || normalized.contains("tron bo") || normalized.contains("chuot va ban phim")
                || normalized.contains("chuot va phim") || normalized.contains("chuot + phim")
                || normalized.contains("phim + chuot") || (normalized.contains("chuot") && normalized.contains("phim"))) {
            intent.isCombo = true;
        } else if (normalized.contains("chuot") || normalized.contains("mouse")) {
            intent.categoryFocus = "chuot";
        } else if (normalized.contains("ban phim") || normalized.contains("phim") || normalized.contains("keyboard") || normalized.contains("switch") || normalized.contains("keycap")) {
            intent.categoryFocus = "ban-phim";
        } else if (normalized.contains("tai nghe") || normalized.contains("headset") || normalized.contains("headphone") || normalized.contains("mic")) {
            intent.categoryFocus = "tai-nghe";
        } else if (normalized.contains("lot chuot") || normalized.contains("pad") || normalized.contains("phu kien")) {
            intent.categoryFocus = "phu-kien";
        }

        // Connection type
        if (normalized.contains("khong day") || normalized.contains("wireless") || normalized.contains("bluetooth")
                || normalized.contains("2.4g") || normalized.contains("2.4ghz") || normalized.contains("tri mode") || normalized.contains("trimode")) {
            intent.isWireless = true;
        }
        if (normalized.contains("co day") || normalized.contains("wired") || normalized.contains("cam day") || normalized.contains("type c")) {
            intent.isWired = true;
        }

        // Brand preference
        String[] brands = {"akko", "logitech", "razer", "hyperwork", "corsair", "dareu", "pulsar", "acer", "asus", "steelseries", "msi"};
        for (String b : brands) {
            if (normalized.contains(b)) {
                intent.brandFocus = b;
                break;
            }
        }

        // Game type
        if (normalized.contains("fps") || normalized.contains("cs2") || normalized.contains("csgo")
                || normalized.contains("valorant") || normalized.contains("pubg") || normalized.contains("apex")
                || normalized.contains("ban sung") || normalized.contains("overwatch") || normalized.contains("cod")
                || normalized.contains("crossfire") || normalized.contains("dot kich")) {
            intent.isFps = true;
        }
        if (normalized.contains("moba") || normalized.contains("lol") || normalized.contains("lien minh")
                || normalized.contains("dota") || normalized.contains("lien quan") || normalized.contains("toc chien")) {
            intent.isMoba = true;
        }
        if (normalized.contains("van phong") || normalized.contains("go van ban") || normalized.contains("lam viec")
                || normalized.contains("hoc tap") || normalized.contains("silent") || normalized.contains("khong on")
                || normalized.contains("yen tinh") || normalized.contains("em ai")) {
            intent.isOffice = true;
        }

        // Hand size / form
        if (normalized.contains("tay nho") || normalized.contains("tay be") || normalized.contains("nho gon")
                || normalized.contains("sieu nhe") || normalized.contains("lightweight") || normalized.contains("nhe")
                || normalized.contains("form nho") || normalized.contains("form be")) {
            intent.isSmallHand = true;
        }
        if (normalized.contains("tay to") || normalized.contains("tay lon") || normalized.contains("dam tay")
                || normalized.contains("form to") || normalized.contains("form lon")) {
            intent.isLargeHand = true;
        }
        if (normalized.contains("cong thai hoc") || normalized.contains("ergo") || normalized.contains("do co tay")) {
            intent.isErgonomic = true;
        }

        // Budget detection
        intent.budget = extractBudget(normalized);

        return intent;
    }

    private BigDecimal extractBudget(String text) {
        // Pattern 1: "1.5tr", "1,5tr", "1tr5", "1.5 trieu", "1.5 triệu"
        Pattern p1 = Pattern.compile("(\\d+)(?:[.,](\\d+))?\\s*(?:tr|trieu|triệu)");
        Matcher m1 = p1.matcher(text);
        if (m1.find()) {
            double major = Double.parseDouble(m1.group(1));
            double minor = m1.group(2) != null ? Double.parseDouble("0." + m1.group(2)) : 0;
            return BigDecimal.valueOf((long) ((major + minor) * 1_000_000));
        }

        // Pattern 2: "1tr5"
        Pattern p2 = Pattern.compile("(\\d+)tr(\\d+)");
        Matcher m2 = p2.matcher(text);
        if (m2.find()) {
            double major = Double.parseDouble(m2.group(1));
            double minor = Double.parseDouble("0." + m2.group(2));
            return BigDecimal.valueOf((long) ((major + minor) * 1_000_000));
        }

        // Pattern 3: "500k", "800k", "1500k"
        Pattern p3 = Pattern.compile("(\\d+)\\s*k");
        Matcher m3 = p3.matcher(text);
        if (m3.find()) {
            long k = Long.parseLong(m3.group(1));
            return BigDecimal.valueOf(k * 1_000);
        }

        // Pattern 4: raw number e.g. "1500000" or "1.500.000"
        Pattern p4 = Pattern.compile("(\\d{1,3}(?:[.]\\d{3})+|\\d{6,9})\\s*(?:vnd|d|đ)?");
        Matcher m4 = p4.matcher(text);
        if (m4.find()) {
            String raw = m4.group(1).replace(".", "");
            return new BigDecimal(raw);
        }

        return null;
    }

    private boolean isCategoryMatching(String catSlug, String focus) {
        if (catSlug == null || focus == null) return false;
        String c = catSlug.toLowerCase(Locale.ROOT);
        String f = focus.toLowerCase(Locale.ROOT);
        if (c.equals(f)) return true;
        return switch (f) {
            case "chuot", "mouse" -> c.contains("chuot") || c.contains("mouse");
            case "ban-phim", "keyboard", "phim" -> c.contains("phim") || c.contains("keyboard");
            case "tai-nghe", "headset", "headphone" -> c.contains("tai") || c.contains("head");
            case "phu-kien" -> c.contains("phu-kien") || c.contains("pad");
            default -> c.contains(f);
        };
    }

    private ScoredProduct scoreProduct(Product product, Intent intent, int stock) {
        int score = 0;
        StringBuilder reason = new StringBuilder();

        String catSlug = product.getCategory() == null ? "" : product.getCategory().getSlug().toLowerCase(Locale.ROOT);
        String name = product.getName() == null ? "" : product.getName();
        String desc = product.getDescription() == null ? "" : product.getDescription();
        String specs = extractSpecsString(product.getSpecifications());
        String fullText = removeAccents((name + " " + desc + " " + specs).toLowerCase(Locale.ROOT));

        BigDecimal price = product.getEffectivePrice();

        // 1. Category check
        if (intent.isCombo) {
            if (isCategoryMatching(catSlug, "chuot") || isCategoryMatching(catSlug, "ban-phim")) {
                score += 50;
            } else {
                return new ScoredProduct(product, 0, "");
            }
        } else if (intent.categoryFocus != null) {
            if (isCategoryMatching(catSlug, intent.categoryFocus)) {
                score += 60;
            } else {
                return new ScoredProduct(product, 0, "");
            }
        } else {
            // General query: give slight preference to core gear
            if (isCategoryMatching(catSlug, "chuot") || isCategoryMatching(catSlug, "ban-phim") || isCategoryMatching(catSlug, "tai-nghe")) {
                score += 30;
            }
        }

        // 2. Wireless vs Wired check
        boolean hasWireless = fullText.contains("khong day") || fullText.contains("wireless")
                || fullText.contains("bluetooth") || fullText.contains("2.4ghz") || fullText.contains("2.4g")
                || fullText.contains("tri-mode") || fullText.contains("trimode") || fullText.contains("lightspeed");
        boolean hasWired = fullText.contains("co day") || fullText.contains("wired") || fullText.contains("type-c");

        if (intent.isWireless) {
            if (hasWireless) {
                score += 50;
                appendReason(reason, "Kết nối không dây tự do, độ trễ cực thấp");
            } else {
                return new ScoredProduct(product, 0, "");
            }
        } else if (intent.isWired) {
            if (hasWired && !hasWireless) {
                score += 40;
                appendReason(reason, "Kết nối có dây ổn định, không lo sạc pin");
            } else if (hasWireless) {
                score -= 30;
            }
        }

        // 3. Brand match
        if (intent.brandFocus != null) {
            String brandName = product.getBrand() == null ? "" : removeAccents(product.getBrand().getName().toLowerCase(Locale.ROOT));
            if (brandName.contains(intent.brandFocus) || fullText.contains(intent.brandFocus)) {
                score += 40;
                appendReason(reason, "Thương hiệu " + (product.getBrand() != null ? product.getBrand().getName() : intent.brandFocus.toUpperCase(Locale.ROOT)) + " uy tín");
            }
        }

        // 4. Game type match
        if (intent.isFps) {
            if (isCategoryMatching(catSlug, "chuot")) {
                if (fullText.contains("dpi") || fullText.contains("sensor") || fullText.contains("mat doc")
                        || fullText.contains("ips") || fullText.contains("nhe") || fullText.contains("fps") || fullText.contains("dash")) {
                    score += 30;
                    appendReason(reason, "Cảm biến độ chính xác cao chuẩn FPS");
                }
            } else if (isCategoryMatching(catSlug, "ban-phim")) {
                if (fullText.contains("red") || fullText.contains("linear") || fullText.contains("silver") || fullText.contains("tkl") || fullText.contains("75%")) {
                    score += 30;
                    appendReason(reason, "Switch phản hồi nhanh, layout gọn tối ưu không gian vẩy chuột");
                }
            } else if (isCategoryMatching(catSlug, "tai-nghe")) {
                if (fullText.contains("7.1") || fullText.contains("spatial") || fullText.contains("am vom") || fullText.contains("tieng dong")) {
                    score += 30;
                    appendReason(reason, "Tái tạo âm thanh đa hướng nhận diện rõ tiếng bước chân");
                }
            }
        }

        if (intent.isMoba) {
            if (isCategoryMatching(catSlug, "chuot")) {
                score += 25;
                appendReason(reason, "Nút bấm nảy, độ bền switch cao cho tần suất click liên tục");
            }
        }

        if (intent.isOffice) {
            if (fullText.contains("silent") || fullText.contains("khong on") || fullText.contains("em")
                    || fullText.contains("brown") || fullText.contains("bluetooth") || fullText.contains("wireless") || fullText.contains("m840") || fullText.contains("silentium")) {
                score += 35;
                appendReason(reason, "Gõ êm ái, cách âm tốt không gây ồn văn phòng");
            }
        }

        // 5. Hand size / form match
        if (intent.isSmallHand && isCategoryMatching(catSlug, "chuot")) {
            if (fullText.contains("sieu nhe") || fullText.contains("lightweight") || fullText.contains("mini")
                    || fullText.contains("nho") || fullText.contains("dash") || fullText.contains("v9") || fullText.contains("g102") || fullText.contains("viper mini")) {
                score += 30;
                appendReason(reason, "Form nhỏ gọn siêu nhẹ, kiểm soát chuột linh hoạt không mỏi tay");
            }
        } else if (intent.isLargeHand && isCategoryMatching(catSlug, "chuot")) {
            if (fullText.contains("dam tay") || fullText.contains("to") || fullText.contains("lon") || fullText.contains("deathadder") || fullText.contains("comfort") || fullText.contains("m840")) {
                score += 25;
                appendReason(reason, "Form ôm tay đầm chắc, hỗ trợ bàn tay cỡ lớn");
            }
        }

        if (intent.isErgonomic && (fullText.contains("cong thai hoc") || fullText.contains("ergo") || fullText.contains("comfort"))) {
            score += 25;
            appendReason(reason, "Thiết kế công thái học nâng đỡ cổ tay tự nhiên");
        }

        // 6. Budget check
        if (intent.budget != null) {
            BigDecimal maxForSingle = intent.isCombo ? intent.budget.multiply(new BigDecimal("0.7")) : intent.budget;
            if (price.compareTo(maxForSingle) <= 0) {
                score += 40;
                if (intent.budget.compareTo(BigDecimal.ZERO) > 0) {
                    double ratio = price.doubleValue() / intent.budget.doubleValue();
                    if (ratio >= 0.55 && ratio <= 1.0) {
                        score += 20;
                    }
                }
                appendReason(reason, "Đúng ngân sách dưới " + formatMoney(intent.budget));
            } else {
                BigDecimal overAmount = price.subtract(intent.budget);
                if (overAmount.compareTo(new BigDecimal("150000")) <= 0) {
                    score -= 35;
                } else {
                    score -= 100;
                }
            }
        }

        // 7. Stock & discount bonus
        if (stock >= 5) {
            score += 5;
        }
        if (product.getSalePrice() != null && product.getSalePrice().compareTo(product.getBasePrice()) < 0) {
            score += 10;
            appendReason(reason, "Đang có ưu đãi giá sốc");
        }

        if (reason.isEmpty()) {
            reason.append("Sản phẩm chính hãng chất lượng cao, độ bền vượt trội");
        }

        return new ScoredProduct(product, score, reason.toString());
    }

    private List<ScoredProduct> pickOptimalCombo(List<ScoredProduct> scored, BigDecimal budget) {
        List<ScoredProduct> mice = scored.stream().filter(sp -> isCategoryMatching(sp.product.getCategory().getSlug(), "chuot")).toList();
        List<ScoredProduct> keyboards = scored.stream().filter(sp -> isCategoryMatching(sp.product.getCategory().getSlug(), "ban-phim")).toList();

        if (mice.isEmpty() && keyboards.isEmpty()) {
            return Collections.emptyList();
        }
        if (mice.isEmpty()) {
            return List.of(keyboards.getFirst());
        }
        if (keyboards.isEmpty()) {
            return List.of(mice.getFirst());
        }

        // Find pair satisfying budget if possible
        ScoredProduct bestMouse = null;
        ScoredProduct bestKeyboard = null;
        int bestPairScore = -1;

        for (ScoredProduct m : mice) {
            for (ScoredProduct k : keyboards) {
                BigDecimal sum = m.product.getEffectivePrice().add(k.product.getEffectivePrice());
                if (budget == null || sum.compareTo(budget) <= 0) {
                    int totalScore = m.score + k.score;
                    if (totalScore > bestPairScore) {
                        bestPairScore = totalScore;
                        bestMouse = m;
                        bestKeyboard = k;
                    }
                }
            }
        }

        if (bestMouse != null && bestKeyboard != null) {
            return List.of(bestMouse, bestKeyboard);
        }

        // Fallback: pick the lowest price pair or simply the highest scored pair
        return List.of(mice.getFirst(), keyboards.getFirst());
    }

    private List<ScoredProduct> pickTopProducts(List<ScoredProduct> scored, BigDecimal budget, int limit) {
        if (budget != null) {
            List<ScoredProduct> withinBudget = scored.stream()
                    .filter(sp -> sp.product.getEffectivePrice().compareTo(budget) <= 0)
                    .limit(limit)
                    .toList();
            if (!withinBudget.isEmpty()) {
                return withinBudget;
            }
        }
        return scored.stream().limit(limit).toList();
    }

    private RecommendedProduct toRecommendedProductView(Product product, int score, String reason, int stock, Store store) {
        String primaryImage = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImage = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .map(ProductImage::getSecureUrl)
                    .findFirst()
                    .orElse(product.getImages().getFirst().getSecureUrl());
        }

        String fullReason = reason + " • Sẵn hàng tại " + store.getName();

        return new RecommendedProduct(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory() == null ? "" : product.getCategory().getName(),
                product.getCategory() == null ? "" : product.getCategory().getSlug(),
                product.getBrand() == null ? "" : product.getBrand().getName(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getEffectivePrice(),
                primaryImage,
                stock,
                score,
                fullReason);
    }

    private String buildConsultantReply(Intent intent, Store store, List<RecommendedProduct> products, BigDecimal totalEstimate) {
        if (products.isEmpty()) {
            return "Rất tiếc hiện tại chi nhánh " + store.getName() + " chưa có sẵn sản phẩm phù hợp hoàn toàn với yêu cầu này. Bạn có thể thử tăng ngân sách hoặc kiểm tra các chi nhánh khác của UTEGear nhé!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Chào bạn! Dựa trên nhu cầu ");
        if (intent.isFps) sb.append("chiến game FPS (CS2/Valorant) ");
        if (intent.isMoba) sb.append("leo rank MOBA (LMHT/Dota 2) ");
        if (intent.isOffice) sb.append("làm việc văn phòng êm ái ");
        if (intent.isSmallHand) sb.append("với form tay nhỏ ");
        if (intent.budget != null) {
            sb.append("và ngân sách khoảng ").append(formatMoney(intent.budget)).append(" ");
        }
        sb.append("tại chi nhánh **").append(store.getName()).append("**, UTEGear đề xuất ");
        if (intent.isCombo && products.size() >= 2) {
            sb.append("**combo tối ưu sau:**\n\n");
        } else {
            sb.append("**lựa chọn tốt nhất dưới đây:**\n\n");
        }

        for (int i = 0; i < products.size(); i++) {
            RecommendedProduct p = products.get(i);
            sb.append(i + 1).append(". **").append(p.name()).append("** - Giá: **").append(formatMoney(p.effectivePrice())).append("**\n");
            sb.append("   - *Ưu điểm:* ").append(p.matchReason()).append("\n");
        }

        sb.append("\nTất cả sản phẩm trên đều **đang sẵn hàng tại quầy trải nghiệm ").append(store.getName()).append("**. Bạn có thể bấm Xem chi tiết bên dưới để xem thông số và đặt mua nhé!");

        return sb.toString();
    }

    private List<String> buildNextSuggestions(Intent intent, Store store) {
        List<String> list = new ArrayList<>();
        if (intent.isCombo) {
            list.add("Nâng ngân sách lên 2 triệu xem combo cao cấp hơn");
            list.add("Tư vấn riêng chuột không dây chơi FPS");
        } else if (isCategoryMatching(intent.categoryFocus, "chuot")) {
            list.add("Gợi ý thêm bàn phím cơ phù hợp để thành combo");
            list.add("Tìm chuột không dây cùng tầm giá");
        } else if (isCategoryMatching(intent.categoryFocus, "ban-phim")) {
            list.add("Gợi ý thêm chuột gaming đồng bộ");
            list.add("Xem bàn phím layout 75% gọn gàng");
        } else {
            list.add("Tư vấn combo chuột + phím FPS dưới 1.5 triệu");
            list.add("Kiểm tra tồn kho chi nhánh khác");
        }
        return list;
    }

    private String extractSpecsString(JsonNode specs) {
        if (specs == null || !specs.isObject()) return "";
        StringBuilder sb = new StringBuilder();
        JsonNode technical = specs.path("technicalSpecifications");
        if (technical.isObject()) {
            technical.fields().forEachRemaining(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue().asText()).append(" "));
        }
        return sb.toString();
    }

    private void appendReason(StringBuilder sb, String text) {
        if (!sb.isEmpty()) sb.append(", ");
        sb.append(text);
    }

    private String formatMoney(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount) + "₫";
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d").replace("Đ", "D");
    }

    private static class Intent {
        boolean isCombo;
        String categoryFocus;
        boolean isWireless;
        boolean isWired;
        String brandFocus;
        boolean isFps;
        boolean isMoba;
        boolean isOffice;
        boolean isSmallHand;
        boolean isLargeHand;
        boolean isErgonomic;
        BigDecimal budget;

        String toDisplaySummary() {
            List<String> parts = new ArrayList<>();
            if (isCombo) {
                parts.add("Combo Chuột + Phím");
            } else if (categoryFocus != null) {
                String c = categoryFocus.toLowerCase(Locale.ROOT);
                if (c.contains("chuot") || c.contains("mouse")) parts.add("Chuột Gaming");
                else if (c.contains("phim") || c.contains("keyboard")) parts.add("Bàn phím cơ");
                else if (c.contains("tai") || c.contains("head")) parts.add("Tai nghe Gaming");
            }

            if (isWireless) parts.add("Không dây");
            if (isWired) parts.add("Có dây");
            if (brandFocus != null) parts.add(brandFocus.toUpperCase(Locale.ROOT));

            if (isFps) parts.add("FPS (CS2/Valorant)");
            if (isMoba) parts.add("MOBA (LMHT)");
            if (isOffice) parts.add("Văn phòng/Yên tĩnh");

            if (isSmallHand) parts.add("Tay nhỏ/Siêu nhẹ");
            if (isLargeHand) parts.add("Tay to/Đầm chắc");
            if (isErgonomic) parts.add("Công thái học");

            if (budget != null) {
                parts.add("Ngân sách ≤ " + CURRENCY_FORMAT.format(budget) + "₫");
            }
            return parts.isEmpty() ? "Tư vấn tổng quan" : String.join(" • ", parts);
        }
    }

    private record ScoredProduct(Product product, int score, String reason) {
    }
}

