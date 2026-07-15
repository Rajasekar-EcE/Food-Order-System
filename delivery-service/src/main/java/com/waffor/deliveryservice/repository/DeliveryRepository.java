package com.waffor.deliveryservice.repository;

import com.waffor.deliveryservice.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);
}
