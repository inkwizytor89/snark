package org.enoch.snark.instance.model.action.filter;

import org.enoch.snark.action.command.SendCommand;

import java.util.List;
import java.util.Map;

public abstract class AbstractFilter {

    public abstract List<SendCommand> filter(List<SendCommand> fleets);

    private static AbstractFilter create(String key, String value) {
        String filterTypeString = key.substring(key.indexOf("_") + 1).toUpperCase();
        FilterType filterType = FilterType.valueOf(filterTypeString);
        switch (filterType) {
            case UNUSED_EXPEDITION: return new UnusedExpeditionFilter();
            case HIGHEST_CAPACITY: return new HighestCapacityFilter();
        }
        throw new IllegalStateException("Can not map "+key+" to "+FilterType.class.getName());
    }

    public static List<AbstractFilter> create(List<Map.Entry<String, String>> conditionsEntry) {
        return conditionsEntry.stream()
                .map(entry -> create(entry.getKey(), entry.getValue()))
                .toList();
    }
}
