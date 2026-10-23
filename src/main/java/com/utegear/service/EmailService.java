package com.utegear.service;

public interface EmailService {

    void sendRegistrationOtp(String toEmail, String fullName, String otpCode);

    void sendPasswordResetOtp(String toEmail, String fullName, String otpCode);
}
