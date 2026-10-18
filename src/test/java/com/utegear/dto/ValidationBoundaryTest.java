package com.utegear.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.utegear.dto.admin.AdminDtos;
import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.cart.CartItemRequest;
import com.utegear.dto.cart.CartQuantityRequest;
import com.utegear.dto.order.AddressRequest;
import com.utegear.dto.order.CheckoutItemRequest;
import com.utegear.dto.support.SupportChatDtos;
import com.utegear.entity.DiscountType;
import com.utegear.entity.ProductStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ValidationBoundaryTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequestFullNameLengthBoundaries() {
        // min-1 (1 char) -> invalid
        assertThat(validator.validate(new RegisterRequest("A", "a@example.com", "0900000001", "password123")))
                .isNotEmpty();
        // min (2 chars) -> valid
        assertThat(validator.validate(new RegisterRequest("An", "a@example.com", "0900000001", "password123")))
                .isEmpty();
        // max (100 chars) -> valid
        assertThat(validator.validate(new RegisterRequest("A".repeat(100), "a@example.com", "0900000001", "password123")))
                .isEmpty();
        // max+1 (101 chars) -> invalid
        assertThat(validator.validate(new RegisterRequest("A".repeat(101), "a@example.com", "0900000001", "password123")))
                .isNotEmpty();
    }

    @Test
    void registerRequestPasswordLengthBoundaries() {
        // min-1 (7 chars) -> invalid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "0900000001", "1234567")))
                .isNotEmpty();
        // min (8 chars) -> valid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "0900000001", "12345678")))
                .isEmpty();
        // max (72 chars) -> valid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "0900000001", "a".repeat(72))))
                .isEmpty();
        // max+1 (73 chars) -> invalid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "0900000001", "a".repeat(73))))
                .isNotEmpty();
    }

    @Test
    void registerRequestPhoneBoundaries() {
        // min-1 (9 digits) -> invalid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "090000001", "password123")))
                .isNotEmpty();
        // min (10 digits) -> valid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "0900000001", "password123")))
                .isEmpty();
        // max (20 digits) -> valid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "1".repeat(20), "password123")))
                .isEmpty();
        // max+1 (21 digits) -> invalid
        assertThat(validator.validate(new RegisterRequest("Nguyen An", "a@example.com", "1".repeat(21), "password123")))
                .isNotEmpty();
    }

    @Test
    void cartItemRequestQuantityBoundaries() {
        // min-1 (0) -> invalid
        assertThat(validator.validate(new CartItemRequest(1L, 0))).isNotEmpty();
        // min (1) -> valid
        assertThat(validator.validate(new CartItemRequest(1L, 1))).isEmpty();
        // max (99) -> valid
        assertThat(validator.validate(new CartItemRequest(1L, 99))).isEmpty();
        // max+1 (100) -> invalid
        assertThat(validator.validate(new CartItemRequest(1L, 100))).isNotEmpty();
    }

    @Test
    void cartQuantityRequestBoundaries() {
        assertThat(validator.validate(new CartQuantityRequest(0))).isNotEmpty();
        assertThat(validator.validate(new CartQuantityRequest(1))).isEmpty();
        assertThat(validator.validate(new CartQuantityRequest(99))).isEmpty();
        assertThat(validator.validate(new CartQuantityRequest(100))).isNotEmpty();
    }

    @Test
    void checkoutItemRequestQuantityBoundaries() {
        assertThat(validator.validate(new CheckoutItemRequest(1L, 0))).isNotEmpty();
        assertThat(validator.validate(new CheckoutItemRequest(1L, 1))).isEmpty();
        assertThat(validator.validate(new CheckoutItemRequest(1L, 99))).isEmpty();
        assertThat(validator.validate(new CheckoutItemRequest(1L, 100))).isNotEmpty();
    }

    @Test
    void addressRequestFullNameBoundaries() {
        // 1 char -> invalid
        assertThat(validator.validate(new AddressRequest("A", "0900000001", "01 Nguyen Hue", "Phuong 1", "Quan 1", "TP.HCM", "Nha")))
                .isNotEmpty();
        // 2 chars -> valid
        assertThat(validator.validate(new AddressRequest("An", "0900000001", "01 Nguyen Hue", "Phuong 1", "Quan 1", "TP.HCM", "Nha")))
                .isEmpty();
        // 100 chars -> valid
        assertThat(validator.validate(new AddressRequest("A".repeat(100), "0900000001", "01 Nguyen Hue", "Phuong 1", "Quan 1", "TP.HCM", "Nha")))
                .isEmpty();
        // 101 chars -> invalid
        assertThat(validator.validate(new AddressRequest("A".repeat(101), "0900000001", "01 Nguyen Hue", "Phuong 1", "Quan 1", "TP.HCM", "Nha")))
                .isNotEmpty();
    }

    @Test
    void productRequestPriceBoundaries() {
        // basePrice below min (999.99) -> invalid
        AdminDtos.ProductRequest belowMin = new AdminDtos.ProductRequest(
                "Chuot Test", 1L, null, null, new BigDecimal("999.99"), null,
                ProductStatus.ACTIVE, "SKU-TEST", Map.of(), JsonNodeFactory.instance.objectNode());
        assertThat(validator.validate(belowMin)).isNotEmpty();

        // basePrice at min (1000.00) -> valid
        AdminDtos.ProductRequest atMin = new AdminDtos.ProductRequest(
                "Chuot Test", 1L, null, null, new BigDecimal("1000.00"), null,
                ProductStatus.ACTIVE, "SKU-TEST", Map.of(), JsonNodeFactory.instance.objectNode());
        assertThat(validator.validate(atMin)).isEmpty();

        // basePrice at max (999999999.99) -> valid
        AdminDtos.ProductRequest atMax = new AdminDtos.ProductRequest(
                "Chuot Test", 1L, null, null, new BigDecimal("999999999.99"), null,
                ProductStatus.ACTIVE, "SKU-TEST", Map.of(), JsonNodeFactory.instance.objectNode());
        assertThat(validator.validate(atMax)).isEmpty();

        // basePrice above max (1000000000.00) -> invalid
        AdminDtos.ProductRequest aboveMax = new AdminDtos.ProductRequest(
                "Chuot Test", 1L, null, null, new BigDecimal("1000000000.00"), null,
                ProductStatus.ACTIVE, "SKU-TEST", Map.of(), JsonNodeFactory.instance.objectNode());
        assertThat(validator.validate(aboveMax)).isNotEmpty();
    }

    @Test
    void couponRequestBoundaries() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        // discountValue at min 0.01 -> valid
        AdminDtos.CouponRequest validCoupon = new AdminDtos.CouponRequest(
                "CP01", DiscountType.FIXED_AMOUNT, new BigDecimal("0.01"), BigDecimal.ZERO, null,
                now, now.plusDays(1), 0, true);
        assertThat(validator.validate(validCoupon)).isEmpty();

        // discountValue 0.00 -> invalid
        AdminDtos.CouponRequest zeroValue = new AdminDtos.CouponRequest(
                "CP02", DiscountType.FIXED_AMOUNT, BigDecimal.ZERO, BigDecimal.ZERO, null,
                now, now.plusDays(1), 0, true);
        assertThat(validator.validate(zeroValue)).isNotEmpty();

        // maxUses > 1,000,000 -> invalid
        AdminDtos.CouponRequest overUses = new AdminDtos.CouponRequest(
                "CP03", DiscountType.FIXED_AMOUNT, new BigDecimal("10000"), BigDecimal.ZERO, null,
                now, now.plusDays(1), 1000001, true);
        assertThat(validator.validate(overUses)).isNotEmpty();
    }

    @Test
    void inventoryRequestBoundaries() {
        assertThat(validator.validate(new AdminDtos.InventoryQuantityRequest(-1))).isNotEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryQuantityRequest(0))).isEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryQuantityRequest(1000000))).isEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryQuantityRequest(1000001))).isNotEmpty();

        assertThat(validator.validate(new AdminDtos.InventoryTransferRequest(1L, 2L, 10L, 0))).isNotEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryTransferRequest(1L, 2L, 10L, 1))).isEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryTransferRequest(1L, 2L, 10L, 1000000))).isEmpty();
        assertThat(validator.validate(new AdminDtos.InventoryTransferRequest(1L, 2L, 10L, 1000001))).isNotEmpty();
    }

    @Test
    void supportChatSendMessageRequestBoundaries() {
        assertThat(validator.validate(new SupportChatDtos.SendMessageRequest(""))).isNotEmpty();
        assertThat(validator.validate(new SupportChatDtos.SendMessageRequest("a"))).isEmpty();
        assertThat(validator.validate(new SupportChatDtos.SendMessageRequest("a".repeat(2000)))).isEmpty();
        assertThat(validator.validate(new SupportChatDtos.SendMessageRequest("a".repeat(2001)))).isNotEmpty();
    }
}

