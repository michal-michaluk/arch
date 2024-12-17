package devices.configuration.intervals;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class IntervalRulesDocumentRepository implements IntervalRulesRepository {

    @Override
    public IntervalRules get() {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public void save(IntervalRules configuration) {
        throw new NotImplementedException("Not implemented yet");
    }
}
