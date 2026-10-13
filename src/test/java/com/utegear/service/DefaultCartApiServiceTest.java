package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Cart;
import com.utegear.entity.User;
import com.utegear.repository.StoreInventoryRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCartApiServiceTest {

    private CartService cartService;
    private StoreInventoryRepository inventoryRepository;
    private DefaultCartApiService service;
    private User user;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        inventoryRepository = mock(StoreInventoryRepository.class);
        service = new DefaultCartApiService(cartService, inventoryRepository);
        user = mock(User.class);
    }

    @Test
    void mapsEmptyCartWithoutInventoryQuery() {
        Cart cart = mock(Cart.class);
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-10-13T01:00:00Z");
        when(cart.getItems()).thenReturn(List.of());
        when(cart.getId()).thenReturn(3L);
        when(cart.getSubtotal()).thenReturn(BigDecimal.ZERO);
        when(cart.getUpdatedAt()).thenReturn(updatedAt);
        when(cartService.getCart(user)).thenReturn(cart);

        var response = service.get(user);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.items()).isEmpty();
        verify(cartService).getCart(user);
    }

    @Test
    void delegatesRemovalToDomainService() {
        service.remove(user, 9L);
        verify(cartService).removeItem(user, 9L);
    }

    @Test
    void delegatesClearToDomainService() {
        service.clear(user);
        verify(cartService).clear(user);
    }
}
