package com.waffor.paymentservice.controller;

import com.waffor.paymentservice.model.Payment;
import com.waffor.paymentservice.model.PaymentRequest;
import com.waffor.paymentservice.repository.PaymentRepository;
import com.waffor.paymentservice.service.PaymentProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Lets you exercise payment-service on its own — no Camunda process instance required.
 * Note: hitting this endpoint directly does NOT advance any running order workflow,
 * since no external task was dispatched. It's for isolated testing/demo only.
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentProcessingService paymentProcessingService, PaymentRepository paymentRepository) {
        this.paymentProcessingService = paymentProcessingService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody PaymentRequest request) {
        Payment result = paymentProcessingService.process(request.getOrderId(), request.getAmount(), false);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long orderId) {
        return paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Payment>> listPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}
