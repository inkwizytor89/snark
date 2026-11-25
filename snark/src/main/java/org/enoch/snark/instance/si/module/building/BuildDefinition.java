package org.enoch.snark.instance.si.module.building;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

@Data
public class BuildDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "build";
    public static final String LIST = "list";
    public static final String SWAP_TRANSPORT = "swap_transport";
    public static final String SOURCE = "source";
    public static final String LEAVE_MOON_RESOURCES = "leave_moon_resources";
    public static final String LEAVE_PLANET_RESOURCES = "leave_planet_resources";
    public static final String DEFAULT_LIST = "small";

    private String list;
    private Boolean swapTransport;
    private String source;
    private Resources leaveMoonResources;
    private Resources leavePlanetResources;

    public BuildDefinition() {
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
            case LIST -> list = value;
            case SWAP_TRANSPORT -> swapTransport = value == null ? null : Boolean.valueOf(value);
            case SOURCE -> source = value;
            case BuildDefinition.LEAVE_MOON_RESOURCES -> leaveMoonResources = parseResources(value);
            case BuildDefinition.LEAVE_PLANET_RESOURCES -> leavePlanetResources = parseResources(value);
            default -> {
            }
        }
    }

    private Resources parseResources(String value) {
        return value == null ? null : Resources.parse(value);
    }

    public Resources getLeaveResources(ColonyType type, Resources defaultResources) {
        Resources resources = ColonyType.PLANET.equals(type) ? leavePlanetResources : leaveMoonResources;
        return resources == null ? defaultResources : resources;
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition(LIST, DEFAULT_LIST, false,
                "Build list applied on each colony to decide which structures are ordered next."));
        configs.add(new ConfigDefinition(SWAP_TRANSPORT, "true", false,
                "Allow resource transport between colonies before building if the local colony is short on materials."));
        configs.add(new ConfigDefinition(SOURCE, CoordinateExpressionService.PLANETS, false,
                "Colony coordinate expression that selects where to build."));
        configs.add(new ConfigDefinition(BuildDefinition.LEAVE_PLANET_RESOURCES, null, false,
                "Resources to leave on each planet when calculating builds and resource transports."));
        configs.add(new ConfigDefinition(BuildDefinition.LEAVE_MOON_RESOURCES, null, false,
                "Resources to leave on each moon when calculating builds and resource transports."));
        return new ThreadDefinition(THREAD_TYPE, THREAD_TYPE,
                "Builds structures from a configured colony build sequence and resource strategy.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = switch (config.getKey()) {
                case LIST -> list;
                case SWAP_TRANSPORT -> swapTransport == null ? null : swapTransport.toString();
                case SOURCE -> source;
                case LEAVE_PLANET_RESOURCES -> formatResources(leavePlanetResources);
                case LEAVE_MOON_RESOURCES -> formatResources(leaveMoonResources);
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

    private String formatResources(Resources value) {
        if (value == null) {
            return null;
        }
        if (value.metal == 0L && value.crystal == 0L && value.deuterium == 0L) {
            return "nothing";
        }
        if (value.metal == -100L && value.crystal == -100L && value.deuterium == -100L) {
            return "everything";
        }
        return "m" + value.metal + " c" + value.crystal + " d" + value.deuterium;
    }
}
