package org.enoch.snark.instance.si.module.defense;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

@Data
public class DefenseDefinition {

    public static final String THREAD_TYPE = "defense";
    public static final String ALARM = "alarm";
    public static final String LIMIT = "limit";
    public static final String RECALL = "recall";
    public static final String EXAMPLE_TIME = "example_time";
    public static final String EXAMPLE_COORDINATE = "example_coordinate";
    public static final String EXAMPLE_ATTACKER_COORDINATE = "example_attacker_coordinate";

    private String alarm;
    private Long limit;
    private String recall;
    private String exampleTime;
    private String exampleCoordinate;
    private String exampleAttackerCoordinate;

    public DefenseDefinition() {
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
            case ALARM -> alarm = value;
            case LIMIT -> limit = value == null ? null : Long.valueOf(value);
            case RECALL -> recall = value;
            case EXAMPLE_TIME -> exampleTime = value;
            case EXAMPLE_COORDINATE -> exampleCoordinate = value;
            case EXAMPLE_ATTACKER_COORDINATE -> exampleAttackerCoordinate = value;
            default -> {
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, ALARM, AlarmSoundPlayer.MISSING_WAV, false,
                "Play the alarm sound when hostile fleets are detected near the colony."));
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, LIMIT, "3000", false,
                "Maximum defensive actions allowed before the thread pauses."));
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, RECALL, null, false,
                "Recall duration for retreat fleets. Defaults to a randomized 350-second delay."));
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, EXAMPLE_TIME, null, false,
                "Sample time value used for defensive calculations and tests."));
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, EXAMPLE_COORDINATE, null, false,
                "Example coordinate used for defensive examples."));
        configs.add(new ConfigDefinition("defense", THREAD_TYPE, EXAMPLE_ATTACKER_COORDINATE, null, false,
                "Example coordinate of the attacker used for interception examples."));
        return new ThreadDefinition(THREAD_TYPE,
                "Monitors enemy fleets and reacts with local defense or retreat actions.",
                configs);
    }

    public java.time.LocalTime getExampleTimeValue() {
        return exampleTime == null || exampleTime.isBlank()
                ? null
                : java.time.LocalTime.parse(exampleTime);
    }

    public Planet getExampleCoordinateValue() {
        return Planet.parse(exampleCoordinate);
    }

    public Planet getExampleAttackerCoordinateValue() {
        return Planet.parse(exampleAttackerCoordinate);
    }

    public Duration getRecallValue() {
        return recall == null || recall.isBlank() ? null : new Duration(recall);
    }
}
