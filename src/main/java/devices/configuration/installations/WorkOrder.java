package devices.configuration.installations;

import devices.configuration.device.Ownership;
import jakarta.validation.constraints.NotNull;

record WorkOrder(@NotNull String orderId, @NotNull Ownership ownership) {
}
