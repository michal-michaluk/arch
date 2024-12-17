package devices.configuration.search;

import devices.configuration.communication.BootNotification;
import devices.configuration.communication.DeviceStatuses;
import devices.configuration.device.DeviceConfiguration;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@AllArgsConstructor
class DevicesReadModel {


    @EventListener
    public void projectionOf(DeviceConfiguration details) {
        throw new NotImplementedException("Not implemented");
    }

    @EventListener
    public void projectionOf(BootNotification boot) {
        throw new NotImplementedException("Not implemented");
    }

    @EventListener
    public void projectionOf(DeviceStatuses statuses) {
        throw new NotImplementedException("Not implemented");
    }

    @Transactional(readOnly = true)
    public Optional<DeviceDetails> queryDetails(String deviceId) {
        throw new NotImplementedException("Not implemented");
    }

    @Transactional(readOnly = true)
    public List<DevicePin> queryPins(String operator) {
        throw new NotImplementedException("Not implemented");
    }

    @Transactional(readOnly = true)
    public Page<DeviceSummary> querySummary(String operator, Pageable pageable) {
        throw new NotImplementedException("Not implemented");
    }

}
