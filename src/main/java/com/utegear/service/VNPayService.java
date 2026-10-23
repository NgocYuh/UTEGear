package com.utegear.service;

import com.utegear.config.VNPayConfig;
import com.utegear.dto.payment.VNPayCallbackResult;
import com.utegear.dto.payment.VNPayIpnResponse;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Payment;
import com.utegear.entity.PaymentStatus;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.PaymentRepository;
import com.utegear.util.VNPayUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VNPayService {

    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public VNPayService(
            VNPayConfig vnPayConfig,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.vnPayConfig = vnPayConfig;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    public String createPaymentUrl(Order order, String clientIp) {
        if (order == null || order.getOrderCode() == null || order.getTotal() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER", "Đơn hàng không hợp lệ để tạo thanh toán VNPay");
        }

        long amount = order.getTotal().multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        String createDate = now.format(DATE_FORMATTER);
        String expireDate = now.plusMinutes(15).format(DATE_FORMATTER);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderCode());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", (clientIp == null || clientIp.isBlank()) ? "127.0.0.1" : clientIp);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        return VNPayUtil.buildPaymentUrl(params, vnPayConfig.getPayUrl(), vnPayConfig.getHashSecret());
    }

    public boolean validateSignature(Map<String, String> params) {
        if (params == null || !params.containsKey("vnp_SecureHash")) {
            return false;
        }
        String secureHash = params.get("vnp_SecureHash");
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String calculatedHash = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret());
        return calculatedHash.equalsIgnoreCase(secureHash);
    }

    @Transactional
    public VNPayCallbackResult processReturn(Map<String, String> params) {
        boolean validSignature = validateSignature(params);
        String orderCode = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");
        String responseCode = params.get("vnp_ResponseCode");
        String amountStr = params.get("vnp_Amount");

        BigDecimal amount = BigDecimal.ZERO;
        if (amountStr != null && !amountStr.isBlank()) {
            try {
                amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        if (!validSignature) {
            log.warn("VNPay return invalid signature for order: {}", orderCode);
            return new VNPayCallbackResult(false, orderCode, transactionNo, amount, bankCode, "97", "Chữ ký dữ liệu không hợp lệ");
        }

        if (orderCode == null || orderCode.isBlank()) {
            return new VNPayCallbackResult(false, null, transactionNo, amount, bankCode, "01", "Không tìm thấy mã đơn hàng");
        }

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            return new VNPayCallbackResult(false, orderCode, transactionNo, amount, bankCode, "01", "Đơn hàng không tồn tại");
        }

        Payment payment = order.getPayment();
        boolean isSuccess = "00".equals(responseCode);

        if (isSuccess) {
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.markPaid(transactionNo);
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.transitionTo(OrderStatus.CONFIRMED);
                }
                paymentRepository.save(payment);
                orderRepository.save(order);
                log.info("VNPay payment completed for order {}", orderCode);
            }
            return new VNPayCallbackResult(true, orderCode, transactionNo, amount, bankCode, "00", "Giao dịch thanh toán thành công");
        } else {
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.markFailed();
                paymentRepository.save(payment);
                log.warn("VNPay payment failed for order {}, response code: {}", orderCode, responseCode);
            }
            String message = getResponseMessage(responseCode);
            return new VNPayCallbackResult(false, orderCode, transactionNo, amount, bankCode, responseCode, message);
        }
    }

    @Transactional
    public VNPayIpnResponse processIpn(Map<String, String> params) {
        if (!validateSignature(params)) {
            return VNPayIpnResponse.invalidChecksum();
        }

        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            return VNPayIpnResponse.orderNotFound();
        }

        // Kiểm tra số tiền
        long expectedAmount = order.getTotal().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        long receivedAmount = Long.parseLong(amountStr);
        if (expectedAmount != receivedAmount) {
            return VNPayIpnResponse.invalidAmount();
        }

        Payment payment = order.getPayment();
        if (payment == null) {
            return VNPayIpnResponse.otherError("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return VNPayIpnResponse.orderAlreadyConfirmed();
        }

        if ("00".equals(responseCode)) {
            payment.markPaid(transactionNo);
            if (order.getStatus() == OrderStatus.PENDING) {
                order.transitionTo(OrderStatus.CONFIRMED);
            }
            paymentRepository.save(payment);
            orderRepository.save(order);
            log.info("VNPay IPN confirmed payment for order {}", orderCode);
        } else {
            payment.markFailed();
            paymentRepository.save(payment);
            log.warn("VNPay IPN failed payment for order {}", orderCode);
        }

        return VNPayIpnResponse.success();
    }

    public String getResponseMessage(String responseCode) {
        if (responseCode == null) return "Giao dịch không thành công";
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10" -> "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch.";
            case "12" -> "Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13" -> "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin vui lòng thực hiện lại giao dịch.";
            case "24" -> "Khách hàng hủy giao dịch.";
            case "51" -> "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default -> "Giao dịch không thành công (Mã lỗi: " + responseCode + ")";
        };
    }
}

