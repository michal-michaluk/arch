package devices.configuration.search;

import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static devices.configuration.device.DeviceFixture.givenPublicDeviceConfiguration;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeviceReadsController.class)
class DeviceReadsControllerTest {

    @Autowired
    private MockMvc rest;
    @MockitoBean
    private DevicesReadModel projection;

    @Test
    void findById() throws Exception {
        Mockito.when(projection.queryDetails(eq("device-id")))
                .thenReturn(Optional.of(new DeviceDetails(
                        givenPublicDeviceConfiguration("device-id"),
                        CommunicationFixture.boot("device-id")
                )));

        rest.perform(get("/devices/{deviceId}", "device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "deviceId": "device-id",
                          "ownership": {
                            "operator": "Devicex.nl",
                            "provider": "public-devices"
                          },
                          "location": {
                            "street": "Rakietowa",
                            "houseNumber": "1A",
                            "city": "Wrocław",
                            "postalCode": "54-621",
                            "state": null,
                            "country": "POL",
                            "coordinates": {
                              "longitude": 51.09836221719513,
                              "latitude": 16.931752852309156
                            }
                          },
                          "openingHours": {
                            "alwaysOpen": true
                          },
                          "settings": {
                            "autoStart": false,
                            "remoteControl": false,
                            "billing": false,
                            "reimbursement": false,
                            "showOnMap": true,
                            "publicAccess": true
                          },
                          "violations": {
                            "operatorNotAssigned": false,
                            "providerNotAssigned": false,
                            "locationMissing": false,
                            "showOnMapButMissingLocation": false,
                            "showOnMapButNoPublicAccess": false
                          },
                          "visibility": {
                            "roamingEnabled": true,
                            "forCustomer": "USABLE_AND_VISIBLE_ON_MAP"
                          },
                          "boot": {
                            "protocol": "IoT16",
                            "vendor": "Garo",
                            "model": "CPF25 Family",
                            "serial": "820394A93203",
                            "firmware": "1.1"
                          }
                        }
                        """, true));

        Mockito.verify(projection).queryDetails("device-id");
    }

    @Test
    void getSummary() throws Exception {
        Mockito.when(projection.querySummary(any(), any()))
                .thenReturn(page(new DeviceSummary(
                        "device-id",
                        DeviceFixture.location(),
                        List.of("Available", "Faulted")
                )));

        rest.perform(get("/devices?operator={operator}&page=0&size=2", "Devicex.nl")
                        .with(jwt())
                        .accept("application/vnd.device.summary+json"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "content": [
                            {
                              "deviceId": "device-id",
                              "location": {
                                "street": "Rakietowa",
                                "houseNumber": "1A",
                                "city": "Wrocław",
                                "postalCode": "54-621",
                                "state": null,
                                "country": "POL",
                                "coordinates": {
                                  "longitude": 51.09836221719513,
                                  "latitude": 16.931752852309156
                                }
                              },
                              "statuses": [
                                "Available",
                                "Faulted"
                              ]
                            }
                          ],
                          "totalPages": 1,
                          "totalElements": 1,
                          "size": 1,
                          "page": 0
                        }
                        """, true));

        Mockito.verify(projection).querySummary("Devicex.nl", Pageable.ofSize(2));
    }

    @NotNull
    private static PageImpl<DeviceSummary> page(DeviceSummary... summary) {
        return new PageImpl<>(List.of(summary), Pageable.ofSize(2), 1);
    }
}
