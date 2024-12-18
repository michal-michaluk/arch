package devices.configuration.installations;

import devices.configuration.device.DeviceFixture;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static devices.configuration.installations.InstallationProcessState.State.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InstallationController.class)
class InstallationControllerTest {

    @Autowired
    private MockMvc rest;
    @MockitoBean
    private InstallationService service;

    @Test
    void get() throws Exception {
        givenReadModel(state("order-id", "device-id", DEVICE_ASSIGNED));

        get("order-id")
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "DEVICE_ASSIGNED"
                        }
                        """, JsonCompareMode.STRICT
                ));

        verify(service).getByOrderId("order-id");
    }

    @Test
    void getPage() throws Exception {
        givenReadModel(
                state("order-1", "device-id", DEVICE_ASSIGNED),
                state("order-2", "device-id", DEVICE_ASSIGNED));

        get1Page()
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "content": [
                            {
                              "orderId": "order-1",
                              "deviceId": "device-id",
                              "state": "DEVICE_ASSIGNED"
                            },
                            {
                              "orderId": "order-2",
                              "deviceId": "device-id",
                              "state": "DEVICE_ASSIGNED"
                            }
                          ],
                          "totalPages": 1,
                          "totalElements": 2,
                          "page":0,
                          "size":2
                        }
                        """, JsonCompareMode.STRICT
                ));

        verify(service).query(params(), Pageable.ofSize(10));

    }

    @NotNull
    private static InstallationService.QueryParams params() {
        return new InstallationService.QueryParams(List.of());
    }

    @Test
    void noBody() throws Exception {
        rest.perform(MockMvcRequestBuilders.patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void noCommand() throws Exception {
        rest.perform(MockMvcRequestBuilders.patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void assignDevice() throws Exception {
        given(state("order-id", "device-id", DEVICE_ASSIGNED));

        patch(
                """
                        {
                         "assignDevice": "device-id"
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json(
                        """
                                {
                                  "orderId": "order-id",
                                  "deviceId": "device-id",
                                  "state": "DEVICE_ASSIGNED"
                                }
                                """, JsonCompareMode.STRICT));

        verify(service).assignDevice("order-id", "device-id");
    }

    @Test
    void assignLocation() throws Exception {
        given(state("order-id", "device-id", DEVICE_ASSIGNED));

        patch(
                """
                        {
                          "assignLocation": {
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
                          }
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "DEVICE_ASSIGNED"
                        }
                        """, JsonCompareMode.STRICT));

        verify(service).assignLocation("order-id", DeviceFixture.location());
    }

    @Test
    void confirmBootData() throws Exception {
        given(state("order-id", "device-id", BOOTED));

        patch(
                """
                        {
                          "confirmBoot": true
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "BOOTED"
                        }
                        """, JsonCompareMode.STRICT));

        verify(service).confirmBootData("order-id");
    }

    @Test
    void wrongConfirmBootData() throws Exception {
        rest.perform(MockMvcRequestBuilders.patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmBoot": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void complete() throws Exception {
        given(state("order-id", "device-id", COMPLETED));

        patch(
                """
                        {
                          "complete": true
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "COMPLETED"
                        }
                        """, JsonCompareMode.STRICT
                ));

        verify(service).complete("order-id");
    }

    @Test
    void wrongComplete() throws Exception {
        rest.perform(MockMvcRequestBuilders.patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "complete": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private ResultActions get(String orderId) throws Exception {
        return rest.perform(MockMvcRequestBuilders.get("/installations/{orderId}", orderId)
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions get1Page() throws Exception {
        return rest.perform(MockMvcRequestBuilders.get("/installations?page=0&size=10")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions patch(@Language("JSON") String request) throws Exception {
        return rest.perform(MockMvcRequestBuilders.patch("/installations/{orderId}", "order-id")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));
    }

    private void given(InstallationProcessState state) {
        Mockito.when(service.getByOrderId(state.orderId()))
                .thenReturn(Optional.of(state));
    }

    private void givenReadModel(InstallationProcessState state) {
        Mockito.when(service.getByOrderId(state.orderId()))
                .thenReturn(Optional.of(state));
    }

    private void givenReadModel(InstallationProcessState... states) {
        Mockito.when(service.query(Mockito.any(), Mockito.any()))
                .thenReturn(page(List.of(states)));
    }

    private Page<InstallationProcessState> page(List<InstallationProcessState> states) {
        return new PageImpl<>(states);
    }

    @NotNull
    private static InstallationProcessState state(String orderId, String deviceId, InstallationProcessState.State state) {
        return new InstallationProcessState(orderId, deviceId, state);
    }
}
