package org.enoch.snark.instance.si.module.scan;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScanDefinition {

    public static final String THREAD_TYPE = "scan";
    public static final String COORDINATE = "coordinate";
    public static final String RANGE = "range";
    public static final String EXPLORATION_AREA = "exploration_area";

    private String coordinate;
    private String range;
    private String explorationArea;

    public ScanDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs()) {
            update(config.getKey(), config.getValue());
        }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            update(config.key, config.value);
        }
    }

    private void update(String key, String value) {
        switch (key) {
            case COORDINATE -> coordinate = value;
            case RANGE -> range = value;
            case EXPLORATION_AREA -> explorationArea = value;
            default -> {
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("scan", THREAD_TYPE, COORDINATE, "", false,
                "Coordinate used as the origin for the scan range."));
        configs.add(new ConfigDefinition("scan", THREAD_TYPE, RANGE, "", false,
                "Scan radius around the configured coordinate or system."));
        configs.add(new ConfigDefinition("scan", THREAD_TYPE, EXPLORATION_AREA, "", false,
                "System area selected for discovery scans and exploration."));
        return new ThreadDefinition(THREAD_TYPE,
                "Finds unscanned targets within a configured area and prepares them for follow-up actions.",
                configs);
    }
}
