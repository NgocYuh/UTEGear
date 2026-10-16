package com.utegear.repository;

import com.utegear.entity.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("""
            select user from User user join user.roles role
            where user.assignedStore.id = :storeId and user.enabled = true and role.name = :roleName
            order by user.id asc
            """)
    List<User> findEnabledByAssignedStoreIdAndRoleName(
            @Param("storeId") Long storeId, @Param("roleName") String roleName);
}
