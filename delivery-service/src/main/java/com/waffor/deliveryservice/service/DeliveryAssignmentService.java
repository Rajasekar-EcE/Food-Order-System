package com.waffor.deliveryservice.service;

import com.waffor.deliveryservice.model.Delivery;
import com.waffor.deliveryservice.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DeliveryAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAssignmentService.class);
    private static final List<String> DRIVERS = List.of("Priya", "Arjun", "Meera", "Karthik", "Divya");

    private final DeliveryRepository deliveryRepository;
    private final RestTemplate restTemplate;

    @Value("${order-service.base-url}")
    private String orderServiceBaseUrl;

    public DeliveryAssignmentService(DeliveryRepository deliveryRepository, RestTemplate restTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.restTemplate = restTemplate;
    }

    public Delivery assign(Long orderId, boolean pushStatusToOrderService) {
        if (pushStatusToOrderService) {
            updateOrderStatus(orderId, "OUT_FOR_DELIVERY");
        }

        String driver = DRIVERS.get(ThreadLocalRandom.current().nextInt(DRIVERS.size()));
        log.info("[DeliveryService] Order #{} - Driver {} assigned, delivering...", orderId, driver);

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setDriverName(driver);
        delivery.setStatus("DELIVERED");
        Delivery saved = deliveryRepository.save(delivery);

        log.info("[DeliveryService] Order #{} - Driver {} assigned, delivering... DELIVERED", orderId, driver);

        if (pushStatusToOrderService) {
            updateOrderStatus(orderId, "DELIVERED");
        }

        return saved;
    }

    private void updateOrderStatus(Long orderId, String status) {
        try {
            restTemplate.put(orderServiceBaseUrl + "/api/orders/" + orderId + "/status",
                    Map.of("status", status));
        } catch (Exception e) {
            log.warn("[DeliveryService] Order #{} - Could not push status '{}' to order-service: {}",
                    orderId, status, e.getMessage());
        }
    }
}
