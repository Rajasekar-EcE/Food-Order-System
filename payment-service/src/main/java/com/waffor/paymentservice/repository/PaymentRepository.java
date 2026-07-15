package com.waffor.paymentservice.repository;

import com.waffor.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);
}
