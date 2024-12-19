package devices.configuration.installations;

import devices.configuration.device.Ownership;
import devices.configuration.tools.JsonConfiguration;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
class WorkOrdersListener {

    public static final String TOPIC_SALES_WORK_ORDERS = "sales.work-orders";

    private final ApplicationEventPublisher publisher;

    @KafkaListener(topics = TOPIC_SALES_WORK_ORDERS)
    void listenWorkOrderMessages(ConsumerRecord<String, String> message) {
        try {
            WorkOrder order = JsonConfiguration.parse(message.value(), WorkOrderMessageV1.class)
                    .toWorkOrder();
            publisher.publishEvent(order);
        } catch (Exception e) {
            // DLC
        }
    }

    record WorkOrderMessageV1(String id, String tenant, String account) {
        WorkOrder toWorkOrder() {
            Objects.requireNonNull(id, "field id is required in message sales.work-orders");
            Objects.requireNonNull(tenant, "field tenant is required in message sales.work-orders");
            Objects.requireNonNull(account, "field account is required in message sales.work-orders");
            return new WorkOrder(id, new Ownership(tenant, account));
        }
    }
}
