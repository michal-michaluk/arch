package devices.configuration.tools;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class KafkaFixture {

    static {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                .withReuse(true)
                .withNetwork(null);
        kafkaContainer.start();

        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }

    @Bean
    NewTopic createStationSnapshotV1Topic() {
        return new NewTopic("example-topic-v1", 1, (short) 1);
    }

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaFixture(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

}
