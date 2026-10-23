package com.utegear.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEmailService.class);

    private final JavaMailSender mailSender;
    private final String mailUsername;
    private final String mailFrom;
    private final boolean devMode;

    public DefaultEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${app.mail.from:no-reply@utegear.vn}") String mailFrom,
            @Value("${app.mail.dev-mode:true}") boolean devMode) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailUsername = mailUsername;
        this.mailFrom = mailFrom;
        this.devMode = devMode;
    }

    @Override
    public void sendRegistrationOtp(String toEmail, String fullName, String otpCode) {
        String subject = "UTEGear - Mã OTP kích hoạt tài khoản của bạn";
        String htmlContent = buildEmailTemplate(
                "KÍCH HOẠT TÀI KHOẢN",
                "Chào " + (fullName != null && !fullName.isBlank() ? fullName : "bạn") + ",",
                "Cảm ơn bạn đã đăng ký tài khoản tại UTEGear. Dưới đây là mã OTP để kích hoạt tài khoản của bạn:",
                otpCode,
                "Mã OTP này có hiệu lực trong vòng <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.");

        logOtpConsole(toEmail, "REGISTRATION_ACTIVATION", otpCode);
        sendMimeEmail(toEmail, subject, htmlContent);
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String fullName, String otpCode) {
        String subject = "UTEGear - Mã OTP đặt lại mật khẩu của bạn";
        String htmlContent = buildEmailTemplate(
                "ĐẶT LẠI MẬT KHẨU",
                "Chào " + (fullName != null && !fullName.isBlank() ? fullName : "bạn") + ",",
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản UTEGear của bạn. Dưới đây là mã xác nhận OTP:",
                otpCode,
                "Mã OTP này có hiệu lực trong vòng <strong>10 phút</strong>. Nếu bạn không yêu cầu đổi mật khẩu, vui lòng bỏ qua email này.");

        logOtpConsole(toEmail, "PASSWORD_RESET", otpCode);
        sendMimeEmail(toEmail, subject, htmlContent);
    }

    private void sendMimeEmail(String toEmail, String subject, String htmlContent) {
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            log.info("[EMAIL SERVICE] Chưa cấu hình SMTP Gmail. Mã OTP đã được in ở console bên trên.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[EMAIL SERVICE] Đã gửi email thành công tới {}", toEmail);
        } catch (Exception e) {
            log.warn("[EMAIL SERVICE] Không thể gửi email qua SMTP: {}. Mã OTP vẫn có hiệu lực và được in ở log console.", e.getMessage());
        }
    }

    private void logOtpConsole(String toEmail, String type, String otpCode) {
        log.info("==================================================================");
        log.info("📧 [UTEGEAR EMAIL OTP] To: {} | Type: {} | Code: [{}]", toEmail, type, otpCode);
        log.info("==================================================================");
    }

    private String buildEmailTemplate(String title, String greeting, String lead, String otpCode, String note) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>UTEGear</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; color: #333;">
                    <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f4f6f9; padding: 30px 0;">
                        <tr>
                            <td align="center">
                                <table width="560" border="0" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.06);">
                                    <!-- HEADER -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #082b63 0%%, #0d387a 100%%); padding: 25px 30px; text-align: center; border-bottom: 3px solid #ed1c24;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: 1px;">UTE<span style="color: #ed1c24;">Gear</span></h1>
                                            <p style="color: #cbd5e1; margin: 5px 0 0 0; font-size: 13px; text-transform: uppercase; letter-spacing: 2px;">%s</p>
                                        </td>
                                    </tr>
                                    <!-- BODY -->
                                    <tr>
                                        <td style="padding: 30px 35px;">
                                            <p style="font-size: 16px; margin-top: 0; font-weight: 600; color: #1e293b;">%s</p>
                                            <p style="font-size: 15px; color: #475569; line-height: 1.6;">%s</p>
                                            
                                            <div style="margin: 30px 0; text-align: center;">
                                                <div style="display: inline-block; background-color: #f8fafc; border: 2px dashed #ed1c24; border-radius: 10px; padding: 16px 36px;">
                                                    <span style="font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #ed1c24; font-family: monospace;">%s</span>
                                                </div>
                                            </div>

                                            <p style="font-size: 14px; color: #64748b; line-height: 1.5; background-color: #f1f5f9; padding: 12px 16px; border-radius: 8px; margin: 0;">
                                                📌 %s
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- FOOTER -->
                                    <tr>
                                        <td style="background-color: #f8fafc; padding: 20px 30px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                                            <p style="margin: 0 0 5px 0;">Hệ thống phân phối Gaming Gear chính hãng UTEGear</p>
                                            <p style="margin: 0;">Email tự động, vui lòng không phản hồi thư này.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(title, greeting, lead, otpCode, note);
    }
}
