package devices.configuration.communication;

import devices.configuration.intervals.IntervalsService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

@Service
@Transactional
@AllArgsConstructor
public class CommunicationService {
    private final Clock clock;
    private final IntervalsService intervals;
    private final ApplicationEventPublisher publisher;

    public BootResponse handleBoot(BootNotification boot) {
        publisher.publishEvent(boot);
        return new BootResponse(
                Instant.now(clock),
                intervals.calculateInterval(boot)
        );
    }

    public void handleStatus(DeviceStatuses status) {
        publisher.publishEvent(status);
    }

    public record BootResponse(Instant serverTime, Duration interval) {

        public <T> T map(Function<BootResponse, T> func) {
            return func.apply(this);
        }

        public int intervalInSeconds() {
            return (int) interval.getSeconds();
        }
    }
}
