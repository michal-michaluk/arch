package devices.configuration.tools;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.net.URI;
import java.util.concurrent.ExecutionException;

@Profile("dynamodb-test")
@Configuration
class DynamoConfiguration {
    private GenericContainer<?> dynamoDB;

    @Bean
    DynamoDbAsyncClient client() throws ExecutionException, InterruptedException {
        dynamoDB = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local:2.5.3"))
                .withReuse(true)
                .withExposedPorts(8000);
        dynamoDB.start();
        var endpointUri = STR."http://\{dynamoDB.getHost()}:\{dynamoDB.getMappedPort(8000)}";
        DynamoDbAsyncClient client = DynamoDbAsyncClient.builder()
                .endpointOverride(URI.create(endpointUri))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create("acc", "sec")))
                .build();

        createConfigTable(client);
        return client;
    }

    @PreDestroy
    public void clean() {
        if (!dynamoDB.isShouldBeReused()) {
            dynamoDB.stop();
        }
    }

    private static void createConfigTable(DynamoDbAsyncClient client) throws InterruptedException, ExecutionException {
        client.deleteTable(table -> table.tableName("Config")).get();
        client.createTable(table -> table
                .tableName("Config")
                .keySchema(KeySchemaElement.builder()
                        .attributeName("name")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("name")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                ).provisionedThroughput(thru -> thru
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L))
        ).get();
    }

}
