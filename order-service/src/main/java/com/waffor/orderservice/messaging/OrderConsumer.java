package com.waffor.orderservice.messaging;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.waffor.orderservice.repository.OrderRepository;
import com.waffor.orderservice.model.Order;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final RuntimeService runtimeService;
    private final OrderRepository orderRepository;

    public OrderConsumer(RuntimeService runtimeService, OrderRepository orderRepository) {
        this.runtimeService = runtimeService;
        this.orderRepository = orderRepository;
    }

    @JmsListener(destination = "${order.queue.name}")
    public void onOrderCreated(OrderCreatedMessage message) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", message.getOrderId());
        vars.put("items", message.getItems());
        vars.put("amount", message.getAmount());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("orderProcess", vars);

        orderRepository.findById(message.getOrderId()).ifPresent(order -> {
            order.setProcessInstanceId(instance.getId());
            orderRepository.save(order);
        });

        log.info("[OrderService] Order #{} - Workflow started (processInstanceId={})",
                message.getOrderId(), instance.getId());
    }
}
