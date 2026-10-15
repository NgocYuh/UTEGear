package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.utegear.entity.Role;
import com.utegear.exception.ApiException;
import com.utegear.repository.RoleRepository;
import com.utegear.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminUserServiceTest {

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(mock(UserRepository.class), mock(RoleRepository.class));
    }

    @Test
    void preventsAdminFromLockingCurrentAccount() {
        assertThatThrownBy(() -> service.changeEnabled(7L, false, 7L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void preventsAdminFromRemovingOwnAdminRole() {
        assertThatThrownBy(() -> service.changeRoles(7L, Set.of(Role.CUSTOMER), 7L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }
}
