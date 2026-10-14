package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.auth.ChangePasswordRequest;
import com.utegear.dto.auth.UpdateProfileRequest;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class ProfileServiceTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private ProfileService service;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new ProfileService(users, encoder);
        user = new User("Nguyễn Văn An", "an@example.test", "0900000001", "old-hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
    }

    @Test
    void updatesOnlyOwnedProfileAndNormalizesPhone() {
        assertThat(service.update(user, new UpdateProfileRequest(" Nguyễn Văn Bình ", " ")).fullName())
                .isEqualTo("Nguyễn Văn Bình");
        assertThat(user.getPhone()).isNull();
        verify(users).save(user);
    }

    @Test
    void rejectsPhoneUsedByAnotherAccount() {
        when(users.existsByPhoneAndIdNot("0900000002", 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(
                user, new UpdateProfileRequest("Nguyễn Văn An", "0900000002")))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("PHONE_ALREADY_EXISTS");
    }

    @Test
    void changesPasswordAfterCheckingCurrentAndConfirmation() {
        when(encoder.matches("current-password", "old-hash")).thenReturn(true);
        when(encoder.matches("new-password", "old-hash")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword(user,
                new ChangePasswordRequest("current-password", "new-password", "new-password"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(users).save(user);
    }

    @Test
    void rejectsMismatchedPasswordConfirmation() {
        when(encoder.matches("current-password", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(user,
                new ChangePasswordRequest("current-password", "new-password", "different-password")))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("VALIDATION_FAILED");
    }
}
