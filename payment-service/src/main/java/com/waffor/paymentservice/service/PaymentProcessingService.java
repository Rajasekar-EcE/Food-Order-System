package com.waffor.paymentservice.service;

import com.waffor.paymentservice.model.Payment;
import com.waffor.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);
    private static final double SUCCESS_RATE = 0.85;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${order-service.base-url}")
    private String orderServiceBaseUrl;

    public PaymentProcessingService(PaymentRepository paymentRepository, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Core payment logic shared by the Camunda external task worker and the direct REST API.
     * pushStatusToOrderService=true mirrors the workflow-driven path (updates the dashboard);
     * set to false when testing this service in isolation without a real order backing it.
     */
    public Payment process(Long orderId, BigDecimal amount, boolean pushStatusToOrderService) {
        if (pushStatusToOrderService) {
            updateOrderStatus(orderId, "PAYMENT_PROCESSING");
        }

        log.info("[PaymentService] Order #{} - Payment processing...", orderId);

        boolean success = ThreadLocalRandom.current().nextDouble() < SUCCESS_RATE;

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus(success ? "SUCCESS" : "FAILED");
        payment.setTransactionId(UUID.randomUUID().toString());
        Payment saved = paymentRepository.save(payment);

        log.info("[PaymentService] Order #{} - Payment processing... {}", orderId, saved.getStatus());

        if (pushStatusToOrderService && !success) {
            updateOrderStatus(orderId, "CANCELLED");
        }

        return saved;
    }

    private void updateOrderStatus(Long orderId, String status) {
        try {
            restTemplate.put(orderServiceBaseUrl + "/api/orders/" + orderId + "/status",
                    Map.of("status", status));
        } catch (Exception e) {
            log.warn("[PaymentService] Order #{} - Could not push status '{}' to order-service: {}",
                    orderId, status, e.getMessage());
        }
    }
}
