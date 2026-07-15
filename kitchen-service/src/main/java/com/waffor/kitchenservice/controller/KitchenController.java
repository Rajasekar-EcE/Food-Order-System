package com.waffor.kitchenservice.controller;

import com.waffor.kitchenservice.model.KitchenTicket;
import com.waffor.kitchenservice.model.KitchenTicketRequest;
import com.waffor.kitchenservice.repository.KitchenTicketRepository;
import com.waffor.kitchenservice.service.KitchenPreparationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Lets you exercise kitchen-service on its own, independent of the Camunda workflow.
 * Direct calls here do not advance any running order process instance.
 */
@RestController
@RequestMapping("/api/kitchen/tickets")
@CrossOrigin(origins = "*")
public class KitchenController {

    private final KitchenPreparationService kitchenPreparationService;
    private final KitchenTicketRepository kitchenTicketRepository;

    public KitchenController(KitchenPreparationService kitchenPreparationService, KitchenTicketRepository kitchenTicketRepository) {
        this.kitchenPreparationService = kitchenPreparationService;
        this.kitchenTicketRepository = kitchenTicketRepository;
    }

    @PostMapping
    public ResponseEntity<KitchenTicket> createTicket(@RequestBody KitchenTicketRequest request) {
        KitchenTicket ticket = kitchenPreparationService.prepare(request.getOrderId(), request.getItems(), false);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<KitchenTicket> getTicket(@PathVariable Long orderId) {
        return kitchenTicketRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<KitchenTicket> updateStatus(@PathVariable Long orderId, @RequestBody StatusOnly body) {
        return kitchenTicketRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(ticket -> {
                    ticket.setStatus(body.getStatus());
                    return ResponseEntity.ok(kitchenTicketRepository.save(ticket));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<KitchenTicket>> listTickets() {
        return ResponseEntity.ok(kitchenTicketRepository.findAll());
    }

    public static class StatusOnly {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
