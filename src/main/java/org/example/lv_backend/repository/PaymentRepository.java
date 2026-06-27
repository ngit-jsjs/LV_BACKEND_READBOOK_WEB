package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByVnpayTxnRef(String txnRef);
}
