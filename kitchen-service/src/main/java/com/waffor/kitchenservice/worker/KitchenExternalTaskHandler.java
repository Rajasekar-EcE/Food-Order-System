package com.waffor.kitchenservice.worker;

import com.waffor.kitchenservice.service.KitchenPreparationService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KitchenExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(KitchenExternalTaskHandler.class);

    @Value("${camunda.client.base-url}")
    private String camundaBaseUrl;

    @Value("${camunda.client.topic}")
    private String topic;

    @Value("${camunda.client.lock-duration}")
    private long lockDuration;

    private final KitchenPreparationService kitchenPreparationService;

    public KitchenExternalTaskHandler(KitchenPreparationService kitchenPreparationService) {
        this.kitchenPreparationService = kitchenPreparationService;
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
            String items = String.valueOf(externalTask.getVariable("items"));

            kitchenPreparationService.prepare(orderId, items, true);

            externalTaskService.complete(externalTask);
        };

        client.subscribe(topic)
                .lockDuration(lockDuration)
                .handler(handler)
                .open();

        log.info("[KitchenService] Subscribed to Camunda external task topic '{}' at {}", topic, camundaBaseUrl);
    }
}
