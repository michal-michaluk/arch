package devices.configuration.device;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
class Device {
    final String deviceId;

    private OpeningHours openingHours;
    private Settings settings;

    void updateOpeningHours(OpeningHours openingHours) {
        openingHours = Objects.requireNonNullElse(openingHours, OpeningHours.alwaysOpen());
        this.openingHours = openingHours;
    }

    void updateSettings(Settings settings) {
        this.settings = this.settings.merge(settings);
    }

}
