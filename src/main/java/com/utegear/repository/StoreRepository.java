package com.utegear.repository;

import com.utegear.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findByActive(boolean active, Pageable pageable);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
