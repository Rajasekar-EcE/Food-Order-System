package com.waffor.kitchenservice.service;

import com.waffor.kitchenservice.model.KitchenTicket;
import com.waffor.kitchenservice.repository.KitchenTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class KitchenPreparationService {

    private static final Logger log = LoggerFactory.getLogger(KitchenPreparationService.class);

    private final KitchenTicketRepository kitchenTicketRepository;
    private final RestTemplate restTemplate;

    @Value("${order-service.base-url}")
    private String orderServiceBaseUrl;

    public KitchenPreparationService(KitchenTicketRepository kitchenTicketRepository, RestTemplate restTemplate) {
        this.kitchenTicketRepository = kitchenTicketRepository;
        this.restTemplate = restTemplate;
    }

    public KitchenTicket prepare(Long orderId, String items, boolean pushStatusToOrderService) {
        if (pushStatusToOrderService) {
            updateOrderStatus(orderId, "KITCHEN_PREP");
        }

        log.info("[KitchenService] Order #{} - Kitchen ticket created, preparing food...", orderId);

        int prepTimeSeconds = ThreadLocalRandom.current().nextInt(5, 21);

        KitchenTicket ticket = new KitchenTicket();
        ticket.setOrderId(orderId);
        ticket.setItems(items);
        ticket.setStatus("READY");
        ticket.setPrepTimeSeconds(prepTimeSeconds);
        KitchenTicket saved = kitchenTicketRepository.save(ticket);

        log.info("[KitchenService] Order #{} - Kitchen ticket created, preparing food... READY", orderId);

        return saved;
    }

    private void updateOrderStatus(Long orderId, String status) {
        try {
            restTemplate.put(orderServiceBaseUrl + "/api/orders/" + orderId + "/status",
                    Map.of("status", status));
        } catch (Exception e) {
            log.warn("[KitchenService] Order #{} - Could not push status '{}' to order-service: {}",
                    orderId, status, e.getMessage());
        }
    }
}
