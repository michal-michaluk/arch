package devices.configuration.intervals;

import devices.configuration.tools.JsonConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Primary
@AllArgsConstructor
class IntervalRulesDynamoRepository implements IntervalRulesRepository {

    public static final String TABLE = "Config";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_CONFIGURATION = "configuration";
    public static final String CONFIG_NAME = "IntervalRules";

    private final DynamoDbAsyncClient client;

    @Override
    public IntervalRules get() {
        try {
            return client.getItem(GetItemRequest.builder()
                            .tableName(TABLE)
                            .key(Map.of(
                                    ATTR_NAME, AttributeValue.builder().s(CONFIG_NAME).build()
                            )).build())
                    .thenApply(getItemResponse -> getItemResponse.item().get(ATTR_CONFIGURATION))
                    .thenApply(configuration -> {
                        if (configuration == null || configuration.nul()) {
                            return IntervalRules.defaultRules();
                        } else {
                            return JsonConfiguration.parse(configuration.s(), IntervalRules.class);
                        }
                    })
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(IntervalRules configuration) {
        try {
            client.putItem(PutItemRequest.builder()
                            .tableName(TABLE)
                            .item(Map.of(
                                    ATTR_NAME, AttributeValue.builder().s(CONFIG_NAME).build(),
                                    ATTR_CONFIGURATION, AttributeValue.builder().s(JsonConfiguration.json(configuration)).build()
                            ))
                            .build())
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
