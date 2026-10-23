package com.utegear.repository;

import com.utegear.entity.EmailOtp;
import com.utegear.entity.OtpType;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpType otpType);

    Optional<EmailOtp> findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, OtpType otpType, OffsetDateTime now);

    void deleteByEmailIgnoreCaseAndOtpType(String email, OtpType otpType);
}
