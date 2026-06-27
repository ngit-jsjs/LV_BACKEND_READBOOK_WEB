package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.PlanResponse;
import org.example.lv_backend.mapper.PlanMapper;
import org.example.lv_backend.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(planMapper::toPlanResponse)
                .toList();
    }
}
