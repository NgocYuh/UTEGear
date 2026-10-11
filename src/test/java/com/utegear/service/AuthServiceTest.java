package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.RoleRepository;
import com.utegear.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void registersCustomerWithNormalizedEmailAndEncodedPassword() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", " An@Example.com ", "0900000007", "password123");
        when(roleRepository.findByName(Role.CUSTOMER)).thenReturn(Optional.of(new Role(Role.CUSTOMER)));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile profile = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("an@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRoles()).extracting(Role::getName).containsExactly(Role.CUSTOMER);
        assertThat(profile.roles()).containsExactly(Role.CUSTOMER);
    }

    @Test
    void rejectsExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", "an@example.com", null, "password123");
        when(userRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
                    assertThat(exception.getStatus().value()).isEqualTo(409);
                });
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsExistingPhone() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", "an@example.com", "0900000007", "password123");
        when(userRepository.existsByPhone("0900000007")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo("PHONE_ALREADY_EXISTS"));
        verify(userRepository, never()).save(any());
    }
}
