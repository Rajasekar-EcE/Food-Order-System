package com.waffor.kitchenservice.repository;

import com.waffor.kitchenservice.model.KitchenTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {
    Optional<KitchenTicket> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);
}
