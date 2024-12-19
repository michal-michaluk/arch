package devices.configuration.communication;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devices.configuration.device.Ownership;
import devices.configuration.tools.JsonConfiguration;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Component
@Transactional
@AllArgsConstructor
class KnownDevicesReadModel implements KnownDevices {
    private final JpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public State queryDevice(String deviceId) {
        return repository.findById(deviceId)
                .map(KnownDeviceEntity::state)
                .orElse(State.UNKNOWN);
    }

    @KafkaListener(topics = "installations-events")
    void listenInstallationsMessages(ConsumerRecord<String, String> message) {
        try {
            switch (JsonConfiguration.parse(message.value(), InstallationMessage.class)) {
                case InstallationMessage.DeviceAssignedMessageV1 started ->
                        put(started.deviceId(), State.IN_INSTALLATION);
                case InstallationMessage.InstallationCompletedMessageV1 ended -> put(ended.deviceId(), State.EXISTING);
                case InstallationMessage.NotInterested ignored -> {
                }
            }
        } catch (Exception e) {
            // DLC
        }
    }

    @KafkaListener(topics = "device-configuration")
    void listenDeviceConfigurationMessages(ConsumerRecord<String, String> message) {
        try {
            DeviceConfigurationMessageV1 reconfigured = JsonConfiguration.parse(message.value(), DeviceConfigurationMessageV1.class);
            if (reconfigured.isUnowned()) {
                put(reconfigured.deviceId(), State.UNKNOWN);
            }
        } catch (Exception e) {
            // DLC
        }
    }

    private void put(String deviceId, State state) {
        repository.save(repository.findById(deviceId)
                .orElseGet(() -> new KnownDeviceEntity(deviceId))
                .state(state)
        );
    }

    interface JpaRepository extends CrudRepository<KnownDeviceEntity, String> {
    }

    @Entity
    @Table(name = "known_device")
    @NoArgsConstructor
    static class KnownDeviceEntity {
        @Id
        private String deviceId;
        @Enumerated(STRING)
        private State state;

        KnownDeviceEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        State state() {
            return this.state;
        }

        KnownDeviceEntity state(State state) {
            this.state = state;
            return this;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = InstallationMessage.NotInterested.class, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = InstallationMessage.DeviceAssignedMessageV1.class, name = "DeviceAssigned-v1"),
            @JsonSubTypes.Type(value = InstallationMessage.InstallationCompletedMessageV1.class, name = "InstallationCompleted-v1"),
    })
    sealed interface InstallationMessage {
        record DeviceAssignedMessageV1(String deviceId) implements InstallationMessage {}

        record InstallationCompletedMessageV1(String deviceId) implements InstallationMessage {}

        record NotInterested() implements InstallationMessage {}
    }

    record DeviceConfigurationMessageV1(String deviceId, OwnershipV1 ownership) {
        boolean isUnowned() {
            return Optional.ofNullable(ownership)
                    .map(o -> o.toOwnership().isUnowned())
                    .orElse(true);
        }

        record OwnershipV1(String operator, String provider) {
            Ownership toOwnership() {
                return new Ownership(operator, provider);
            }
        }
    }
}
