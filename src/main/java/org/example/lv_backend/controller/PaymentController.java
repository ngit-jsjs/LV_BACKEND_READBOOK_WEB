package org.example.lv_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.repository.UserRepository;
import org.example.lv_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    @PostMapping("/buy-package")
    public ApiResponse<String> buyPackage(
            @RequestParam Long planId,
            HttpServletRequest httpRequest) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        if (!user.isVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String ipAddress = getClientIp(httpRequest);
        String paymentUrl = paymentService.createPaymentUrl(planId, user, ipAddress);
        return ApiResponse.<String>builder()
                .message("Tạo URL mua gói xu thành công")
                .result(paymentUrl)
                .build();
    }
    @GetMapping("/vnpay-return")
    public RedirectView vnpayReturn(@RequestParam Map<String, String> allRequestParams) throws Exception {
        String rspCode = paymentService.handleCallback(allRequestParams);

        String frontendUrl = "http://localhost:5173/payment/result";
        if ("00".equals(rspCode)) {
            return new RedirectView(frontendUrl + "?status=success");
        } else {
            return new RedirectView(frontendUrl + "?status=failed&error=" + rspCode);
        }
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> allRequestParams) {
        Map<String, String> response = new HashMap<>();
        try {
            String rspCode = paymentService.handleCallback(allRequestParams);
            response.put("RspCode", rspCode);
            response.put("Message", "00".equals(rspCode) ? "Confirm Success" : "Error Code: " + rspCode);
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown Error: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(remoteAddr) ? "127.0.0.1" : remoteAddr;
    }
}
