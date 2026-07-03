package org.example.lv_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.SubStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private SubStatus status;
    private LocalDateTime createdAt;
    private Long planId;
    private String planName;
    private BigDecimal planPrice;
    private Long planAmount;
}
