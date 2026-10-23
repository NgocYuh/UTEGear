package com.utegear.repository;

import com.utegear.entity.TokenBlacklist;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    void deleteByExpiresAtBefore(OffsetDateTime now);
}

