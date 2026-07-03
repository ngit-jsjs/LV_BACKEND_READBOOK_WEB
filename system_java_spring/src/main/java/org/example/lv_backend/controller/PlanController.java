package org.example.lv_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.PlanResponse;
import org.example.lv_backend.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;
    @GetMapping
    public ApiResponse<List<PlanResponse>> getAllPlans() {
        return ApiResponse.<List<PlanResponse>>builder()
                .result(planService.getAllPlans())
                .build();
    }
}
