package devices.configuration.installations;

import devices.configuration.installations.DomainEvent.DeviceAssigned;
import devices.configuration.installations.DomainEvent.InstallationCompleted;
import devices.configuration.installations.DomainEvent.InstallationStarted;
import devices.configuration.tools.JsonConfiguration;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class KafkaPublisher {

    public static final String TOPIC_INSTALLATIONS_EVENTS = "installations-events";
    private final KafkaTemplate<String, String> kafka;

    @EventListener
    public void publish(InstallationStarted event) {
        publish(TOPIC_INSTALLATIONS_EVENTS, event.orderId(), InstallationStartedMessageV1.from(event));
    }

    @EventListener
    public void publish(DeviceAssigned event) {
        publish(TOPIC_INSTALLATIONS_EVENTS, event.orderId(), DeviceAssignedMessageV1.from(event));
    }

    @EventListener
    public void publish(InstallationCompleted event) {
        publish(TOPIC_INSTALLATIONS_EVENTS, event.orderId(), InstallationCompletedMessageV1.from(event));
    }

    private void publish(String topic, String key, Object message) {
        kafka.send(new ProducerRecord<>(topic, key, JsonConfiguration.json(message)));
    }

    record InstallationStartedMessageV1(String type, String orderId) {
        static InstallationStartedMessageV1 from(InstallationStarted event) {
            return new InstallationStartedMessageV1("InstallationStarted-v1", event.orderId());
        }
    }

    record DeviceAssignedMessageV1(String type, String orderId, String deviceId) {
        static DeviceAssignedMessageV1 from(DeviceAssigned event) {
            return new DeviceAssignedMessageV1("DeviceAssigned-v1", event.orderId(), event.deviceId());
        }
    }

    record InstallationCompletedMessageV1(String type, String orderId, String deviceId) {
        static InstallationCompletedMessageV1 from(InstallationCompleted event) {
            return new InstallationCompletedMessageV1("InstallationCompleted-v1", event.orderId(), event.deviceId());
        }
    }
}
