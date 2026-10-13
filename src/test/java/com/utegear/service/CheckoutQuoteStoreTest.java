package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.dto.order.CheckoutItemRequest;
import com.utegear.dto.order.CheckoutRequest;
import com.utegear.dto.order.PlaceOrderRequest;
import com.utegear.entity.PaymentMethod;
import com.utegear.exception.ApiException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckoutQuoteStoreTest {

    private final CheckoutQuoteStore store = new CheckoutQuoteStore();
    private final List<CheckoutItemRequest> items = List.of(new CheckoutItemRequest(10L, 2));

    @Test
    void createsAndRequiresMatchingQuoteWithNormalizedText() {
        var quote = store.create(7L, new CheckoutRequest(
                1L, 2L, " SAVE10 ", PaymentMethod.COD, " note ", items));
        var request = new PlaceOrderRequest(
                quote.token(), 1L, 2L, "SAVE10", PaymentMethod.COD, "note", items);

        assertThat(store.require(7L, request)).isEqualTo(quote);
        assertThat(quote.couponCode()).isEqualTo("SAVE10");
    }

    @Test
    void rejectsQuoteChangedByAnotherUser() {
        var quote = store.create(7L, new CheckoutRequest(
                1L, 2L, null, PaymentMethod.COD, null, items));
        var request = new PlaceOrderRequest(
                quote.token(), 1L, 2L, null, PaymentMethod.COD, null, items);

        assertThatThrownBy(() -> store.require(8L, request))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("QUOTE_CHANGED");
    }

    @Test
    void removedQuoteCannotBeReused() {
        var quote = store.create(7L, new CheckoutRequest(
                1L, 2L, null, PaymentMethod.COD, null, items));
        store.remove(quote.token());
        var request = new PlaceOrderRequest(
                quote.token(), 1L, 2L, null, PaymentMethod.COD, null, items);

        assertThatThrownBy(() -> store.require(7L, request))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("QUOTE_EXPIRED");
    }
}
