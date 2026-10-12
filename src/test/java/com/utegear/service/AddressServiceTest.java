package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.order.AddressCommand;
import com.utegear.entity.Address;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.AddressRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AddressServiceTest {

    private AddressRepository repository;
    private AddressService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(AddressRepository.class);
        service = new AddressService(repository);
        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        when(repository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void makesFirstAddressDefault() {
        when(repository.existsByUserId(7L)).thenReturn(false);

        Address address = service.create(user, command("Nhà"));

        assertThat(address.isDefault()).isTrue();
        verify(repository).save(address);
    }

    @Test
    void switchesDefaultAddressWithinCurrentUser() {
        Address current = address("Nhà");
        Address selected = address("Công ty");
        ReflectionTestUtils.setField(current, "id", 1L);
        ReflectionTestUtils.setField(selected, "id", 2L);
        current.markDefault(true);
        when(repository.findByIdAndUserId(2L, 7L)).thenReturn(Optional.of(selected));
        when(repository.findByUserIdAndDefaultAddressTrue(7L)).thenReturn(Optional.of(current));

        Address result = service.setDefault(user, 2L);

        assertThat(current.isDefault()).isFalse();
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void cannotReadAnotherUsersAddress() {
        when(repository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(user, 99L, command("Nhà")))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("ADDRESS_NOT_FOUND");
    }

    private Address address(String label) {
        return new Address(
                user, "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", label);
    }

    private AddressCommand command(String label) {
        return new AddressCommand(
                "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", label);
    }
}
