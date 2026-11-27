package org.enoch.snark.instance.si.module.space;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Data
public class SpaceDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "space";

    private String coordinate;
    private String range;
    private String spyCoordinate;
    private String spyPattern;
    private Integer galaxyMax;
    private boolean galaxyMaxConfigured;
    private Integer systemMax;
    private Boolean wrapSystem;
    private String expiredTime;

    public SpaceDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs()) {
            update(config.getKey(), config.getValue());
        }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            if (GALAXY_MAX.equals(config.key)) {
                galaxyMaxConfigured = true;
            }
            update(config.key, config.value);
        }
    }

    private void update(String key, String value) {
        switch (key) {
            case COORDINATE -> coordinate = value;
            case RANGE -> range = value;
            case SPY_COORDINATE -> spyCoordinate = value;
            case SPY_PATTERN -> spyPattern = value;
            case GALAXY_MAX -> {
                if (value != null && !value.isBlank()) galaxyMax = Integer.valueOf(value);
            }
            case SYSTEM_MAX -> {
                if (value != null && !value.isBlank()) systemMax = Integer.valueOf(value);
            }
            case WRAP_SYSTEM -> {
                if (value != null && !value.isBlank()) wrapSystem = Boolean.valueOf(value);
            }
            case EXPIRED_TIME -> expiredTime = value;
            default -> {
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition(COORDINATE, "", false,
                "Center coordinate used when the thread scans a custom galaxy/system slice."));
        configs.add(new ConfigDefinition(RANGE, "", false,
                "Distance around the coordinate to include in the space scan."));
        configs.add(new ConfigDefinition(SPY_COORDINATE, "", false,
                "Coordinate used as the source of spy positions during galaxy analysis."));
        configs.add(new ConfigDefinition(SPY_PATTERN, "", false,
                "Pattern applied when matching spy coordinates inside a scanned system."));
        configs.add(new ConfigDefinition(GALAXY_MAX, "0", false,
                "Maximum galaxy number used when scanning the galaxy map."));
        configs.add(new ConfigDefinition(SYSTEM_MAX, "499", false,
                "Maximum system number within each galaxy."));
        configs.add(new ConfigDefinition(WRAP_SYSTEM, "true", false,
                "Wrap system indexing when the range exceeds the maximum system number."));
        configs.add(new ConfigDefinition(EXPIRED_TIME, "P7D", false,
                "Age threshold after which a system record is considered stale and should be rechecked."));
        return new ThreadDefinition(THREAD_TYPE, THREAD_TYPE,
                "Analyzes space ranges, refreshes galaxy data, and schedules follow-up scan commands.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = switch (config.getKey()) {
                case COORDINATE -> coordinate;
                case RANGE -> range;
                case SPY_COORDINATE -> spyCoordinate;
                case SPY_PATTERN -> spyPattern;
                case GALAXY_MAX -> galaxyMax == null ? null : galaxyMax.toString();
                case SYSTEM_MAX -> systemMax == null ? null : systemMax.toString();
                case WRAP_SYSTEM -> wrapSystem == null ? null : wrapSystem.toString();
                case EXPIRED_TIME -> expiredTime;
                default -> config.getValue();
            };
            configs.add(new ConfigDefinition(
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        ThreadDefinition configured = new ThreadDefinition(thread, definition.getType(), definition.getDescription(), configs);
        configured.setModule(module);
        return configured;
    }
}
