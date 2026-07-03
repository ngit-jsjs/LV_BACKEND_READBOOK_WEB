package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.VNPayConfig;
import org.example.lv_backend.dto.response.PaymentResponse;
import org.example.lv_backend.entity.*;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.PaymentMapper;
import org.example.lv_backend.repository.PaymentRepository;
import org.example.lv_backend.repository.PlanRepository;
import org.example.lv_backend.repository.SubscriptionRepository;
import org.example.lv_backend.repository.UserRepository;
import org.example.lv_backend.util.SecurityUtil;
import org.example.lv_backend.util.VNPayUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityUtil securityUtil;
    @Transactional
    public String createPaymentUrl(Long planId, String ipAddress) throws Exception {
        String currentUsername = securityUtil.getCurrentUsername();
        User user = userRepository.findByName(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Gói xu không tồn tại"));
        String vnpTxnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        Payment payment = Payment.builder()
                .amount(plan.getPrice())
                .vnpayTxnRef(vnpTxnRef)
                .status(PaymentStatus.PENDING)
                .plan(plan)
                .user(user)
                .build();
        paymentRepository.save(payment);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", plan.getPrice().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnpTxnRef);
        vnp_Params.put("vnp_OrderInfo", "Nap goi xu: " + plan.getName());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.getPayUrl() + "?" + queryUrl;
    }

    @Transactional
    public String handleCallback(Map<String, String> rawParams) throws Exception {
        Map<String, String> fields = new HashMap<>();

        for (Map.Entry<String, String> entry : rawParams.entrySet()) {
            String fieldName = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }
        String vnp_SecureHash = rawParams.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String calculatedSign = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret());
        if (!calculatedSign.equals(vnp_SecureHash)) {
            return "97";
        }
        String txnRef = rawParams.get("vnp_TxnRef");
        Optional<Payment> paymentOpt = paymentRepository.findByVnpayTxnRef(txnRef);
        if (!paymentOpt.isPresent()) {
            return "01";
        }
        Payment payment = paymentOpt.get();
        double vnpAmount = Double.parseDouble(rawParams.get("vnp_Amount")) / 100;
        if (payment.getAmount().doubleValue() != vnpAmount) {
            return "04";
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return "02";
        }
        String responseCode = rawParams.get("vnp_ResponseCode");
        String transactionStatus = rawParams.get("vnp_TransactionStatus");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            User user = payment.getUser();
            Plan plan = payment.getPlan();
            user.setAmount(user.getAmount().add(BigDecimal.valueOf(plan.getAmount())));
            userRepository.save(user);

            Subscription subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .status(SubStatus.COMPLETED)
                    .build();
            subscriptionRepository.save(subscription);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
        return "00";
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPaymentHistory(int page, int size) {
        String currentUsername = securityUtil.getCurrentUsername();
        User user = userRepository.findByName(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(paymentMapper::toPaymentResponse);
    }


    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByUserAdmin(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }
}
