package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Payment;
import org.example.lv_backend.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByVnpayTxnRef(String txnRef);

    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
