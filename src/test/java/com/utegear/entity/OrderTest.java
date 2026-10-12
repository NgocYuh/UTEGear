package com.utegear.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderTest {

    private User user;
    private Address address;
    private ProductVariant keyboard;
    private ProductVariant mouse;

    @BeforeEach
    void setUp() {
        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        address = new Address(
                user, "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", "Nhà");
        keyboard = variant("Bàn phím K87", "ban-phim-k87", "K87-BLACK", "900000", "50000",
                new LinkedHashMap<>(Map.of("color", "black")));
        mouse = variant("Chuột M1", "chuot-m1", "M1-DEFAULT", "500000", "0", new LinkedHashMap<>());
    }

    @Test
    void calculatesSubtotalDiscountShippingFeeAndTotal() {
        Order order = order();
        order.addItem(keyboard, 2, "https://res.cloudinary.com/demo/k87.jpg");
        order.addItem(mouse, 1, null);
        order.applyPricing(new BigDecimal("100000"), new BigDecimal("30000"));

        assertThat(order.getSubtotal()).isEqualByComparingTo("2400000");
        assertThat(order.getDiscount()).isEqualByComparingTo("100000");
        assertThat(order.getShippingFee()).isEqualByComparingTo("30000");
        assertThat(order.getTotal()).isEqualByComparingTo("2330000");
    }

    @Test
    void snapshotsProductAndVariantAtOrderCreation() {
        Map<String, String> sourceOptions = new LinkedHashMap<>(Map.of("color", "black"));
        ProductVariant variant = variant(
                "Bàn phím K87", "ban-phim-k87", "K87-BLACK", "900000", "50000", sourceOptions);
        OrderItem item = order().addItem(variant, 1, "https://res.cloudinary.com/demo/k87.jpg");

        variant.getOptionValues().put("color", "white");

        assertThat(item.getProductName()).isEqualTo("Bàn phím K87");
        assertThat(item.getSku()).isEqualTo("K87-BLACK");
        assertThat(item.getOptionValues()).containsEntry("color", "black");
        assertThat(item.getUnitPrice()).isEqualByComparingTo("950000");
        assertThat(item.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/k87.jpg");
    }

    @Test
    void rejectsDiscountGreaterThanSubtotal() {
        Order order = order();
        order.addItem(mouse, 1, null);

        assertThatThrownBy(() -> order.applyPricing(new BigDecimal("500001"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createsSinglePaymentForFinalTotal() {
        Order order = order();
        order.addItem(mouse, 1, null);
        order.applyPricing(new BigDecimal("50000"), new BigDecimal("30000"));

        Payment payment = order.createPayment(PaymentMethod.COD);

        assertThat(payment.getAmount()).isEqualByComparingTo("480000");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThatThrownBy(() -> order.createPayment(PaymentMethod.BANK_TRANSFER))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paymentCanOnlyCompleteFromPending() {
        Order order = order();
        order.addItem(mouse, 1, null);
        Payment payment = order.createPayment(PaymentMethod.BANK_TRANSFER);

        payment.markPaid("txn-001");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getTransactionId()).isEqualTo("txn-001");
        assertThat(payment.getPaidAt()).isNotNull();
        assertThatThrownBy(payment::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void followsOrderStatusWorkflowAndRecordsTimestamps() {
        Order order = order();

        order.transitionTo(OrderStatus.CONFIRMED);
        order.transitionTo(OrderStatus.PROCESSING);
        order.transitionTo(OrderStatus.SHIPPING);
        order.transitionTo(OrderStatus.COMPLETED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getConfirmedAt()).isNotNull();
        assertThat(order.getProcessingAt()).isNotNull();
        assertThat(order.getShippedAt()).isNotNull();
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void rejectsSkippedAndTerminalStatusTransitions() {
        Order order = order();

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPING))
                .isInstanceOf(IllegalStateException.class);
        order.transitionTo(OrderStatus.CANCELLED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class);
    }

    private Order order() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Coupon coupon = new Coupon(
                "UTE100", DiscountType.FIXED_AMOUNT, new BigDecimal("100000"),
                BigDecimal.ZERO, null, now.minusDays(1), now.plusDays(1), 0);
        return new Order("UTE-20261012-0001", user, null, coupon, address, ShippingMethod.HOME_DELIVERY, null);
    }

    private ProductVariant variant(
            String name,
            String slug,
            String sku,
            String price,
            String adjustment,
            Map<String, String> options) {
        Product product = new Product(
                name,
                slug,
                new Category("Gear", "gear-" + slug, null, 0),
                null,
                new BigDecimal(price),
                null);
        ProductVariant variant = new ProductVariant(sku, options, true, new BigDecimal(adjustment));
        product.addVariant(variant);
        return variant;
    }
}
