package com.waffor.orderservice.service;

import com.waffor.orderservice.messaging.OrderCreatedMessage;
import com.waffor.orderservice.messaging.OrderProducer;
import com.waffor.orderservice.model.*;
import com.waffor.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderService(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerAddress(request.getCustomerAddress());
        order.setItems(request.getItems());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PLACED);

        Order saved = orderRepository.save(order);
        log.info("[OrderService] Order #{} - Status: PLACED, saved to DB", saved.getId());

        orderProducer.publishOrderCreated(
                new OrderCreatedMessage(saved.getId(), saved.getItems(), saved.getAmount()));

        return saved;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> updateStatus(Long id, OrderStatus status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            Order saved = orderRepository.save(order);
            log.info("[OrderService] Order #{} - Status: {}", saved.getId(), status);
            return saved;
        });
    }
}
