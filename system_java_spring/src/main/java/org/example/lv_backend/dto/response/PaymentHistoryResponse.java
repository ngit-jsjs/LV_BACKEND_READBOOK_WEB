package org.example.lv_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private List<PaymentResponse> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private BigDecimal totalAmount;
    private Long totalCoins;
}
