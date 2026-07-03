package org.example.lv_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.SubscriptionResponse;
import org.example.lv_backend.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    @GetMapping("/my")
    public ApiResponse<List<SubscriptionResponse>> getMySubscriptions() {
        return ApiResponse.<List<SubscriptionResponse>>builder()
                .message("Lấy lịch sử mua gói xu thành công")
                .result(subscriptionService.getMySubscriptions())
                .build();
    }
}
