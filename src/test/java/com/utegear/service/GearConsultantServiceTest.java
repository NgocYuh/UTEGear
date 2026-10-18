package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utegear.dto.consultant.ConsultantDtos.ChatRequest;
import com.utegear.dto.consultant.ConsultantDtos.ChatResponse;
import com.utegear.dto.consultant.ConsultantDtos.QuickPrompt;
import com.utegear.entity.*;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GearConsultantServiceTest {

    private ProductRepository productRepository;
    private StoreRepository storeRepository;
    private StoreInventoryRepository storeInventoryRepository;
    private GearConsultantService service;

    private Store storeDanang;
    private Store storeHcm;
    private Category mouseCategory;
    private Category keyboardCategory;
    private Category headsetCategory;

    @BeforeEach
    void setUp() throws Exception {
        productRepository = mock(ProductRepository.class);
        storeRepository = mock(StoreRepository.class);
        storeInventoryRepository = mock(StoreInventoryRepository.class);
        service = new GearConsultantService(productRepository, storeRepository, storeInventoryRepository);

        storeHcm = createStore(1L, "UTEGear Demo TP.HCM", "TP.HCM", "0909000001");
        storeDanang = createStore(3L, "UTEGear Demo Đà Nẵng", "Đà Nẵng", "0909000003");

        when(storeRepository.findByActiveOrderByNameAsc(true)).thenReturn(List.of(storeHcm, storeDanang));
        when(storeRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(storeDanang));
        when(storeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(storeHcm));

        mouseCategory = new Category("Chuột Gaming", "mouse", "Chuột gaming", 1);
        ReflectionTestUtils.setField(mouseCategory, "id", 10L);

        keyboardCategory = new Category("Bàn phím cơ", "keyboard", "Bàn phím cơ", 2);
        ReflectionTestUtils.setField(keyboardCategory, "id", 20L);

        headsetCategory = new Category("Tai nghe Gaming", "headset", "Tai nghe", 3);
        ReflectionTestUtils.setField(headsetCategory, "id", 30L);
    }

    private Store createStore(Long id, String name, String province, String phone) {
        Store store = new Store(
                name, "01 Demo Street", province, "Hải Châu", phone,
                "store@demo.utegear.test", null, LocalTime.of(8, 0), LocalTime.of(22, 0),
                "Demo store", BigDecimal.ZERO, BigDecimal.ZERO, true);
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }

    @Test
    void getQuickPrompts_returnsCuratedPrompts() {
        List<QuickPrompt> prompts = service.getQuickPrompts();
        assertThat(prompts).isNotEmpty();
        assertThat(prompts).hasSize(4);
        assertThat(prompts.getFirst().title()).contains("FPS");
    }

    @Test
    void consult_recommendsFpsComboUnderBudgetInDanang() throws Exception {
        Product mouseG102 = new Product("Chuột Logitech G102 Lightsync", "chuot-logitech-g102",
                mouseCategory, null, new BigDecimal("450000"), new BigDecimal("399000"));
        ReflectionTestUtils.setField(mouseG102, "id", 101L);
        mouseG102.update("Chuột Logitech G102 Lightsync", "chuot-logitech-g102", mouseCategory, null,
                "Chuột gaming siêu nhẹ 85g form nhỏ đối xứng cảm biến 8000 DPI chuyên game FPS CS2 Valorant",
                new BigDecimal("450000"), new BigDecimal("399000"), ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{\"technicalSpecifications\":{\"Kích thước\":\"Nhỏ gọn\",\"Trọng lượng\":\"85g\"}}"));

        Product keyboardAkko = new Product("Bàn phím Akko 3087 v2 Red Switch", "ban-phim-akko-3087",
                keyboardCategory, null, new BigDecimal("950000"), new BigDecimal("899000"));
        ReflectionTestUtils.setField(keyboardAkko, "id", 102L);
        keyboardAkko.update("Bàn phím Akko 3087 v2 Red Switch", "ban-phim-akko-3087", keyboardCategory, null,
                "Bàn phím cơ TKL layout nhỏ gọn Red switch linear gõ mượt mà phản hồi nhanh cho FPS",
                new BigDecimal("950000"), new BigDecimal("899000"), ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{\"technicalSpecifications\":{\"Switch\":\"Akko V2 Red Linear\"}}"));

        Product expensiveMouse = new Product("Chuột Razer Viper V2 Pro", "chuot-razer-viper-v2-pro",
                mouseCategory, null, new BigDecimal("3200000"), null);
        ReflectionTestUtils.setField(expensiveMouse, "id", 103L);
        expensiveMouse.update("Chuột Razer Viper V2 Pro", "chuot-razer-viper-v2-pro", mouseCategory, null,
                "Chuột gaming cao cấp", new BigDecimal("3200000"), null, ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{}"));

        when(productRepository.findInStockAtStore(eq(3L), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of(mouseG102, keyboardAkko, expensiveMouse));

        ProductVariant v1 = new ProductVariant("SKU-1", Map.of(), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(v1, "product", mouseG102);
        StoreInventory inv1 = new StoreInventory(storeDanang, v1);
        inv1.setQuantity(10);

        ProductVariant v2 = new ProductVariant("SKU-2", Map.of(), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(v2, "product", keyboardAkko);
        StoreInventory inv2 = new StoreInventory(storeDanang, v2);
        inv2.setQuantity(8);

        when(storeInventoryRepository.findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(3L))
                .thenReturn(List.of(inv1, inv2));

        ChatRequest request = new ChatRequest(
                "Tui hay chơi game FPS (CS2/Valorant), tay nhỏ, ngân sách dưới 1.5 triệu, đề xuất combo chuột và phím phù hợp ở chi nhánh Đà Nẵng",
                null);

        ChatResponse response = service.consult(request);

        assertThat(response).isNotNull();
        assertThat(response.store().id()).isEqualTo(3L);
        assertThat(response.store().name()).contains("Đà Nẵng");
        assertThat(response.products()).hasSize(2);
        assertThat(response.products().stream().map(p -> p.categorySlug()).toList())
                .containsExactlyInAnyOrder("mouse", "keyboard");
        assertThat(response.totalEstimate()).isEqualTo(new BigDecimal("1298000"));
        assertThat(response.budget()).isEqualTo(new BigDecimal("1500000"));
        assertThat(response.totalEstimate()).isLessThanOrEqualTo(response.budget());
        assertThat(response.reply()).contains("Logitech G102");
        assertThat(response.reply()).contains("Akko 3087");
        assertThat(response.reply()).contains("Đà Nẵng");
    }

    @Test
    void consult_recommendsOfficeSilentKeyboard() throws Exception {
        Product keyboardSilent = new Product("Bàn phím cơ Silent Office K87", "ban-phim-co-silent-k87",
                keyboardCategory, null, new BigDecimal("850000"), new BigDecimal("750000"));
        ReflectionTestUtils.setField(keyboardSilent, "id", 201L);
        keyboardSilent.update("Bàn phím cơ Silent Office K87", "ban-phim-co-silent-k87", keyboardCategory, null,
                "Bàn phím cơ switch silent êm ái không ồn, phù hợp gõ văn bản và làm việc văn phòng",
                new BigDecimal("850000"), new BigDecimal("750000"), ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{}"));

        when(productRepository.findInStockAtStore(eq(1L), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of(keyboardSilent));

        ProductVariant v = new ProductVariant("SKU-K", Map.of(), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(v, "product", keyboardSilent);
        StoreInventory inv = new StoreInventory(storeHcm, v);
        inv.setQuantity(5);

        when(storeInventoryRepository.findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(1L))
                .thenReturn(List.of(inv));

        ChatRequest request = new ChatRequest("Tìm bàn phím cơ gõ êm văn phòng không ồn", 1L);
        ChatResponse response = service.consult(request);

        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().name()).contains("Silent Office");
        assertThat(response.reply()).contains("văn phòng");
    }

    @Test
    void consult_returnsEmptyListPolitelyWhenNoStock() {
        when(productRepository.findInStockAtStore(eq(3L), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of());

        ChatRequest request = new ChatRequest("Tư vấn chuột gaming ở chi nhánh Đà Nẵng", 3L);
        ChatResponse response = service.consult(request);

        assertThat(response.products()).isEmpty();
        assertThat(response.reply()).contains("chưa có sẵn sản phẩm");
    }

    @Test
    void consult_recommendsWirelessMouseUnderOneMillionAtStoreHcm() throws Exception {
        Category realMouseCategory = new Category("Chuột", "chuot", "Chuột gaming & văn phòng", 2);
        ReflectionTestUtils.setField(realMouseCategory, "id", 2L);

        Product mouseAkkoWireless = new Product("Chuột không dây AKKO Dash V9 Max White", "chuot-khong-day-akko-dash-v9-max-white",
                realMouseCategory, null, new BigDecimal("990000"), null);
        ReflectionTestUtils.setField(mouseAkkoWireless, "id", 40L);
        mouseAkkoWireless.update("Chuột không dây AKKO Dash V9 Max White", "chuot-khong-day-akko-dash-v9-max-white", realMouseCategory, null,
                "Chuột không dây siêu nhẹ kết nối 2.4G và Bluetooth pin trâu độ nhạy cao",
                new BigDecimal("990000"), null, ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{}"));

        Product mouseWired = new Product("Chuột có dây Logitech G102 Black", "chuot-co-day-logitech-g102-black",
                realMouseCategory, null, new BigDecimal("340000"), null);
        ReflectionTestUtils.setField(mouseWired, "id", 44L);
        mouseWired.update("Chuột có dây Logitech G102 Black", "chuot-co-day-logitech-g102-black", realMouseCategory, null,
                "Chuột gaming có dây độ nhạy cao",
                new BigDecimal("340000"), null, ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{}"));

        Product mouseExpensiveWireless = new Product("Chuột gaming không dây Predator Cestus 353", "chuot-predator-cestus-353",
                realMouseCategory, null, new BigDecimal("2500000"), null);
        ReflectionTestUtils.setField(mouseExpensiveWireless, "id", 41L);
        mouseExpensiveWireless.update("Chuột gaming không dây Predator Cestus 353", "chuot-predator-cestus-353", realMouseCategory, null,
                "Chuột không dây cao cấp",
                new BigDecimal("2500000"), null, ProductStatus.ACTIVE,
                new ObjectMapper().readTree("{}"));

        when(productRepository.findInStockAtStore(eq(1L), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of(mouseAkkoWireless, mouseWired, mouseExpensiveWireless));

        ProductVariant vAkko = new ProductVariant("SKU-AKKO", Map.of(), true, BigDecimal.ZERO);
        ReflectionTestUtils.setField(vAkko, "product", mouseAkkoWireless);
        StoreInventory invAkko = new StoreInventory(storeHcm, vAkko);
        invAkko.setQuantity(10);

        when(storeInventoryRepository.findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(1L))
                .thenReturn(List.of(invAkko));

        ChatRequest request = new ChatRequest("chuột không dây dưới 1 triệu", 1L);
        ChatResponse response = service.consult(request);

        assertThat(response.products()).isNotEmpty();
        assertThat(response.products().getFirst().name()).contains("AKKO Dash V9 Max White");
        assertThat(response.products().getFirst().effectivePrice()).isEqualTo(new BigDecimal("990000"));
        assertThat(response.detectedIntent()).contains("Không dây");
        assertThat(response.detectedIntent()).contains("1.000.000");
    }
}

