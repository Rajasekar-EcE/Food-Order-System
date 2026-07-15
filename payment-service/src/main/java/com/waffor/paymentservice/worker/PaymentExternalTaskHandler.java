package com.waffor.paymentservice.worker;

import com.waffor.paymentservice.model.Payment;
import com.waffor.paymentservice.service.PaymentProcessingService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExternalTaskHandler.class);

    @Value("${camunda.client.base-url}")
    private String camundaBaseUrl;

    @Value("${camunda.client.topic}")
    private String topic;

    @Value("${camunda.client.lock-duration}")
    private long lockDuration;

    private final PaymentProcessingService paymentProcessingService;

    public PaymentExternalTaskHandler(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaBaseUrl)
                .asyncResponseTimeout(10000)
                .build();

        ExternalTaskHandler handler = (externalTask, externalTaskService) -> {
            Long orderId = externalTask.getVariable("orderId") instanceof Number
                    ? ((Number) externalTask.getVariable("orderId")).longValue()
                    : Long.valueOf(externalTask.getVariable("orderId").toString());
            Object amountVar = externalTask.getVariable("amount");
            BigDecimal amount = amountVar == null ? BigDecimal.ZERO : new BigDecimal(amountVar.toString());

            Payment result = paymentProcessingService.process(orderId, amount, true);

            externalTaskService.complete(externalTask, Map.of("paymentStatus", result.getStatus()));
        };

        client.subscribe(topic)
                .lockDuration(lockDuration)
                .handler(handler)
                .open();

        log.info("[PaymentService] Subscribed to Camunda external task topic '{}' at {}", topic, camundaBaseUrl);
    }
}
