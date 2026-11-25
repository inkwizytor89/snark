package org.enoch.snark.instance.si.module.fleet;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.common.time.Duration;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.si.module.ThreadConfig;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.to.ShipsMap.*;
import static org.enoch.snark.instance.service.CoordinateExpressionService.PLANETS;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Data
public class FleetDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "fleet";

    private String source;
    private String target;
    private String mission;
    private String shipsWaves;
    private String leaveShipsWaves;
    private String resources;
    private String leaveResources;
    private String speed;
    private String trip;
    private String recall;
    private Boolean dryRun;
    private String queue;
    private String expiredTime;
    private String startConditions;

    public FleetDefinition() {
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
            case SOURCE -> source = value;
            case TARGET -> target = value;
            case MISSION -> mission = value;
            case SHIPS_WAVE -> shipsWaves = value;
            case LEAVE_SHIPS_WAVE -> leaveShipsWaves = value;
            case RESOURCES -> resources = value;
            case LEAVE_RESOURCES -> leaveResources = value;
            case SPEED -> speed = value;
            case TRIP -> trip = value;
            case RECALL -> recall = value;
            case DRY_RUN -> dryRun = value == null ? null : Boolean.valueOf(value);
            case QUEUE -> queue = value;
            case EXPIRED_TIME -> expiredTime = value;
            case START_CONDITION -> startConditions = value;
            default -> {
            }
        }
    }

    public String getEffectiveSource() {
        return source == null || source.isBlank() ? PLANETS : source;
    }

    public String getTargetValue() {
        return target == null || target.isBlank() ? null : target;
    }

    public String getTripValue() {
        return trip == null || trip.isBlank() ? null : trip;
    }

    public Mission getMissionValue() {
        return Mission.convert(mission == null || mission.isBlank() ? Mission.STATIONED.name() : mission);
    }

    public List<ShipsMap> getShipsWavesValue() {
        return parseWaves(shipsWaves, List.of(ALL_SHIPS));
    }

    public List<ShipsMap> getLeaveShipsWavesValue() {
        return parseWaves(leaveShipsWaves, EMPTY_SHIP_WAVE);
    }

    private List<ShipsMap> parseWaves(String value, List<ShipsMap> defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return java.util.Arrays.stream(value.split(ARRAY_SEPARATOR))
                .map(ShipsMap::parse)
                .toList();
    }

    public Resources getResourcesValue() {
        return resources == null || resources.isBlank() ? nothing : Resources.parse(resources);
    }

    public Resources getLeaveResourcesValue() {
        return leaveResources == null || leaveResources.isBlank() ? null : Resources.parse(leaveResources);
    }

    public Long getSpeedValue() {
        return speed == null || speed.isBlank() ? null : Long.valueOf(speed);
    }

    public Duration getRecallValue() {
        return recall == null || recall.isBlank() ? null : new Duration(recall);
    }

    public QueueRunType getQueueValue() {
        return QueueRunType.valueOf(queue == null || queue.isBlank() ? QueueRunType.NORMAL.name() : queue);
    }

    public String getStartConditionsValue() {
        return startConditions == null || startConditions.isBlank() ? "[]" : startConditions;
    }

    public String getExpiredTimeValue() {
        return expiredTime == null || expiredTime.isBlank() ? null : expiredTime;
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition(SOURCE, "", false,
                "Source coordinate expression that defines from which colonies fleets are dispatched."));
        configs.add(new ConfigDefinition(TARGET, "", false,
                "Target coordinate expression used by the fleet plan."));
        configs.add(new ConfigDefinition(MISSION, Mission.STATIONED.name(), false,
                "Mission assigned to the dispatched fleet."));
        configs.add(new ConfigDefinition(SHIPS_WAVE, "", false,
                "Ship waves assigned to each dispatched fleet."));
        configs.add(new ConfigDefinition(LEAVE_SHIPS_WAVE, "", false,
                "Ships to leave behind at the origin colony."));
        configs.add(new ConfigDefinition(RESOURCES, "", false,
                "Resources to include in the fleet payload."));
        configs.add(new ConfigDefinition(LEAVE_RESOURCES, "", false,
                "Resources to leave behind on the origin colony after dispatch."));
        configs.add(new ConfigDefinition(SPEED, "", false,
                "Fleet speed override used during dispatch."));
        configs.add(new ConfigDefinition(TRIP, "", false,
                "Trip route expression used to define the fleet path."));
        configs.add(new ConfigDefinition(RECALL, "", false,
                "Recall delay applied after the fleet is sent."));
        configs.add(new ConfigDefinition(DRY_RUN, "false", false,
                "Only simulate dispatch planning without sending the fleet."));
        configs.add(new ConfigDefinition(QUEUE, QueueRunType.NORMAL.name(), false,
                "Queue priority used when the fleet command is scheduled."));
        configs.add(new ConfigDefinition(EXPIRED_TIME, "", false,
                "Blocking time window used to avoid duplicate fleet dispatches."));
        configs.add(new ConfigDefinition(START_CONDITION, "[]", false,
                "Conditions that must be satisfied before the fleet plan can run."));
        return new ThreadDefinition(THREAD_TYPE,
                "Creates and sends fleet plans according to target, mission, and route configuration.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = switch (config.getKey()) {
                case SOURCE -> source;
                case TARGET -> target;
                case MISSION -> mission;
                case SHIPS_WAVE -> shipsWaves;
                case LEAVE_SHIPS_WAVE -> leaveShipsWaves;
                case RESOURCES -> resources;
                case LEAVE_RESOURCES -> leaveResources;
                case SPEED -> speed;
                case TRIP -> trip;
                case RECALL -> recall;
                case DRY_RUN -> dryRun == null ? null : dryRun.toString();
                case QUEUE -> queue;
                case EXPIRED_TIME -> expiredTime;
                case START_CONDITION -> startConditions;
                default -> config.getValue();
            };
            configs.add(new ConfigDefinition(
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        return new ThreadDefinition(definition.getThread(), definition.getDescription(), configs);
    }
}
