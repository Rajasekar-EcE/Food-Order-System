package com.waffor.deliveryservice.worker;

import com.waffor.deliveryservice.service.DeliveryAssignmentService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeliveryExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryExternalTaskHandler.class);

    @Value("${camunda.client.base-url}")
    private String camundaBaseUrl;

    @Value("${camunda.client.topic}")
    private String topic;

    @Value("${camunda.client.lock-duration}")
    private long lockDuration;

    private final DeliveryAssignmentService deliveryAssignmentService;

    public DeliveryExternalTaskHandler(DeliveryAssignmentService deliveryAssignmentService) {
        this.deliveryAssignmentService = deliveryAssignmentService;
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

            deliveryAssignmentService.assign(orderId, true);

            externalTaskService.complete(externalTask);
        };

        client.subscribe(topic)
                .lockDuration(lockDuration)
                .handler(handler)
                .open();

        log.info("[DeliveryService] Subscribed to Camunda external task topic '{}' at {}", topic, camundaBaseUrl);
    }
}
