package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.ProcessFixture.given;

@Transactional
@IntegrationTest
class InstallationDocumentWithHistoryRepositoryTest {

    @Autowired
    InstallationDocumentWithHistoryRepository repository;

    @Test
    void noProcessFoundByOrderId() {
        Assertions.assertThat(transactional(
                () -> repository.getByOrderId("not-existing")
        )).isEmpty();
    }

    @Test
    void noProcessFoundByDeviceId() {
        Assertions.assertThat(transactional(
                () -> repository.getByDeviceId("not-existing")
        )).isEmpty();
    }

    @Test
    void loadNewProcessByOrderId() {
        InstallationProcess process = given().newProcess();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadProcessByDeviceId() {
        InstallationProcess process = given().withDeviceAssigned();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadAlmostCompletedProcessByDeviceId() {
        InstallationProcess process = given().almostCompleted();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    private void whenProcessIsSaved(InstallationProcess process) {
        transactional(() -> repository.save(process));
    }
}
