package com.waffor.deliveryservice.controller;

import com.waffor.deliveryservice.model.Delivery;
import com.waffor.deliveryservice.model.DeliveryRequest;
import com.waffor.deliveryservice.repository.DeliveryRepository;
import com.waffor.deliveryservice.service.DeliveryAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Lets you exercise delivery-service on its own, independent of the Camunda workflow.
 * Direct calls here do not advance any running order process instance.
 */
@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
public class DeliveryController {

    private final DeliveryAssignmentService deliveryAssignmentService;
    private final DeliveryRepository deliveryRepository;

    public DeliveryController(DeliveryAssignmentService deliveryAssignmentService, DeliveryRepository deliveryRepository) {
        this.deliveryAssignmentService = deliveryAssignmentService;
        this.deliveryRepository = deliveryRepository;
    }

    @PostMapping
    public ResponseEntity<Delivery> assignDelivery(@RequestBody DeliveryRequest request) {
        Delivery delivery = deliveryAssignmentService.assign(request.getOrderId(), false);
        return ResponseEntity.ok(delivery);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Delivery> getDelivery(@PathVariable Long orderId) {
        return deliveryRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Delivery>> listDeliveries() {
        return ResponseEntity.ok(deliveryRepository.findAll());
    }
}
