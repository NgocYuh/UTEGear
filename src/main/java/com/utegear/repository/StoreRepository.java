package com.utegear.repository;

import com.utegear.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findByActive(boolean active, Pageable pageable);

    List<Store> findByActiveOrderByNameAsc(boolean active);

    List<Store> findByActiveAndProvinceIgnoreCaseOrderByNameAsc(boolean active, String province);

    Optional<Store> findByIdAndActiveTrue(Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select store from Store store where store.id = :id")
    Optional<Store> findLockedById(@Param("id") Long id);
}
