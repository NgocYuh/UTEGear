package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.store.StoreActiveRequest;
import com.utegear.dto.store.StoreUpsertRequest;
import com.utegear.entity.Store;
import com.utegear.exception.ApiException;
import com.utegear.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StoreServiceTest {

    private StoreRepository repository;
    private StoreService service;

    @BeforeEach
    void setUp() {
        repository = mock(StoreRepository.class);
        service = new StoreService(repository);
        when(repository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsStoreWithNormalizedOptionalFields() {
        service.create(request(" 0909000001 ", true));

        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(repository).save(captor.capture());
        Store store = captor.getValue();
        assertThat(store.getName()).isEqualTo("UTEGear Quận 1");
        assertThat(store.getPhone()).isEqualTo("0909000001");
        assertThat(store.isActive()).isTrue();
    }

    @Test
    void rejectsDuplicatePhone() {
        when(repository.existsByPhone("0909000001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("0909000001", true)))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void updatesExistingStore() {
        Store store = existingStore();
        when(repository.findById(1L)).thenReturn(Optional.of(store));

        service.update(1L, request("0909000002", true));

        assertThat(store.getPhone()).isEqualTo("0909000002");
        verify(repository).save(store);
    }

    @Test
    void changesStoreActiveState() {
        Store store = existingStore();
        when(repository.findById(1L)).thenReturn(Optional.of(store));

        service.changeActive(1L, new StoreActiveRequest(false));

        assertThat(store.isActive()).isFalse();
        verify(repository).save(store);
    }

    @Test
    void returnsNotFoundForUnknownStore() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeActive(404L, new StoreActiveRequest(false)))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("STORE_NOT_FOUND");
    }

    @Test
    void rejectsEqualOpeningAndClosingTimes() {
        StoreUpsertRequest invalid = new StoreUpsertRequest(
                "UTEGear Quận 1", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức", null,
                null, null, LocalTime.of(8, 0), LocalTime.of(8, 0), null,
                BigDecimal.valueOf(10.85), BigDecimal.valueOf(106.77), true);

        assertThatThrownBy(() -> service.create(invalid))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void findAllValidatesPaginationBounds() {
        assertThatThrownBy(() -> service.findAll(null, -1, 10))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.findAll(null, 0, 0))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.findAll(null, 0, 101))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    private StoreUpsertRequest request(String phone, boolean active) {
        return new StoreUpsertRequest(
                "UTEGear Quận 1", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức", phone,
                "store@utegear.vn", null, LocalTime.of(8, 0), LocalTime.of(21, 0),
                "Chi nhánh chính", BigDecimal.valueOf(10.85), BigDecimal.valueOf(106.77), active);
    }

    private Store existingStore() {
        StoreUpsertRequest request = request("0909000001", true);
        return new Store(
                request.name(), request.address(), request.province(), request.district(), request.phone(),
                request.email(), request.imageUrl(), request.openingTime(), request.closingTime(),
                request.description(), request.latitude(), request.longitude(), request.active());
    }
}
