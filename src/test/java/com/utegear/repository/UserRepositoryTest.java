package com.utegear.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.entity.Role;
import com.utegear.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void persistsUserWithRoleAndAuditTimestamps() {
        Role customer = roleRepository.save(new Role(Role.CUSTOMER));
        User user = user("Customer@Example.com", "0900000001");
        user.getRoles().add(customer);

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly(Role.CUSTOMER);
        assertThat(userRepository.findByEmailIgnoreCase("customer@example.com")).contains(saved);
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(user("duplicate@example.com", "0900000002"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                user("duplicate@example.com", "0900000003")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicatePhone() {
        userRepository.saveAndFlush(user("first@example.com", "0900000004"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                user("second@example.com", "0900000004")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String email, String phone) {
        return new User("Nguyen Van An", email, phone, "encoded-password");
    }
}
