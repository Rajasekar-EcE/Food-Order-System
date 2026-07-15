package com.waffor.orderservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${order.queue.name}")
    private String queueName;

    public OrderProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishOrderCreated(OrderCreatedMessage message) {
        jmsTemplate.convertAndSend(queueName, message);
        log.info("[OrderService] Order #{} - Published to '{}' queue", message.getOrderId(), queueName);
    }
}
