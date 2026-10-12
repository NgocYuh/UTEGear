package com.utegear.service;

import com.utegear.dto.order.CheckoutItemRequest;
import com.utegear.dto.order.CheckoutRequest;
import com.utegear.dto.order.PlaceOrderRequest;
import com.utegear.entity.PaymentMethod;
import com.utegear.exception.ApiException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CheckoutQuoteStore {

    private static final long VALID_MINUTES = 15;
    private final Map<String, Quote> quotes = new ConcurrentHashMap<>();

    public Quote create(Long userId, CheckoutRequest request) {
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(VALID_MINUTES);
        Quote quote = new Quote(
                UUID.randomUUID().toString(), userId, request.addressId(), request.fulfillmentStoreId(),
                normalize(request.couponCode()), request.paymentMethod(), normalize(request.note()),
                List.copyOf(request.items()), expiresAt);
        quotes.put(quote.token(), quote);
        return quote;
    }

    public Quote require(Long userId, PlaceOrderRequest request) {
        Quote quote = quotes.get(request.quoteToken());
        if (quote == null || quote.expiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            quotes.remove(request.quoteToken());
            throw new ApiException(HttpStatus.CONFLICT, "QUOTE_EXPIRED", "Báo giá đã hết hạn, vui lòng tải lại");
        }
        if (!quote.matches(userId, request)) {
            throw new ApiException(HttpStatus.CONFLICT, "QUOTE_CHANGED", "Thông tin đặt hàng đã thay đổi, vui lòng báo giá lại");
        }
        return quote;
    }

    public void remove(String token) {
        quotes.remove(token);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record Quote(
            String token,
            Long userId,
            Long addressId,
            Long storeId,
            String couponCode,
            PaymentMethod paymentMethod,
            String note,
            List<CheckoutItemRequest> items,
            OffsetDateTime expiresAt) {

        boolean matches(Long currentUserId, PlaceOrderRequest request) {
            return userId.equals(currentUserId)
                    && addressId.equals(request.addressId())
                    && storeId.equals(request.fulfillmentStoreId())
                    && java.util.Objects.equals(couponCode, normalize(request.couponCode()))
                    && paymentMethod == request.paymentMethod()
                    && java.util.Objects.equals(note, normalize(request.note()))
                    && items.equals(request.items());
        }
    }
}
