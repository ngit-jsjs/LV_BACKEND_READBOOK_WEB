package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Payment;
import org.example.lv_backend.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByVnpayTxnRef(String txnRef);

    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId AND p.status = org.example.lv_backend.entity.PaymentStatus.SUCCESS")
    BigDecimal sumAmountByUserIdAndStatusSuccess(@Param("userId") Long userId);

    @Query("SELECT SUM(p.plan.amount) FROM Payment p WHERE p.user.id = :userId AND p.status = org.example.lv_backend.entity.PaymentStatus.SUCCESS")
    Long sumCoinsByUserIdAndStatusSuccess(@Param("userId") Long userId);
}
