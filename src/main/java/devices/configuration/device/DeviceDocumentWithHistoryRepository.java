package devices.configuration.device;

import devices.configuration.tools.EventTypes;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
@AllArgsConstructor
class DeviceDocumentWithHistoryRepository implements DeviceRepository {

    private final DocumentRepository documents;
    private final EventRepository events;
    private final ApplicationEventPublisher publisher;

    @Override
    public Optional<Device> get(String deviceId) {
        return documents.findById(deviceId)
                .map(DeviceDocumentEntity::getDevice);
    }

    @Override
    public void save(Device device) {
        List<DomainEvent> emitted = eventsEmittedFrom(device);
        documents.save(documents.findById(device.deviceId)
                .orElseGet(() -> new DeviceDocumentEntity(device.deviceId))
                .setDevice(device)
        );
        emitted.forEach(event -> events.save(
                new DeviceEventEntity(device.deviceId, event)
        ));
        if (!emitted.isEmpty()) {
            publisher.publishEvent(device.toDeviceConfiguration());
        }
        emitted.forEach(publisher::publishEvent);
    }

    private static List<DomainEvent> eventsEmittedFrom(Device device) {
        List<DomainEvent> emitted = List.copyOf(device.events);
        device.events.clear();
        return emitted;
    }

    @Repository
    interface DocumentRepository extends JpaRepository<DeviceDocumentEntity, String> {
    }

    @Entity
    @Table(name = "device_document")
    @NoArgsConstructor
    static class DeviceDocumentEntity {
        @Id
        private String deviceId;
        @Version
        private long version;

        @Getter
        @Type(JsonBinaryType.class)
        private Device device;

        public DeviceDocumentEntity setDevice(Device device) {
            this.device = device;
            return this;
        }

        DeviceDocumentEntity(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    @Repository
    interface EventRepository extends CrudRepository<DeviceEventEntity, UUID> {
    }

    @Entity
    @Table(name = "device_events")
    @NoArgsConstructor
    static class DeviceEventEntity {
        @Id
        private UUID id;
        private String deviceId;
        private String type;
        private Instant time;
        @Type(JsonBinaryType.class)
        private DomainEvent event;

        DeviceEventEntity(String deviceId, DomainEvent event) {
            this.id = UUID.randomUUID();
            this.deviceId = deviceId;
            this.type = EventTypes.of(event).type();
            this.time = Instant.now();
            this.event = event;
        }
    }
}
