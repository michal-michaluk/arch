package devices.configuration.intervals;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
@AllArgsConstructor
class IntervalRulesDynamoRepository implements IntervalRulesRepository {

    public static final String TABLE = "Config";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_CONFIGURATION = "configuration";
    public static final String CONFIG_NAME = "IntervalRules";

    private final DynamoDbClient client;

    @Override
    public IntervalRules get() {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public void save(IntervalRules configuration) {
        throw new NotImplementedException("Not implemented yet");
    }
}
