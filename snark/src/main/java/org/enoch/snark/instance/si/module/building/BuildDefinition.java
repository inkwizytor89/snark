package org.enoch.snark.instance.si.module.building;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

@Data
public class BuildDefinition {

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
        configs.add(new ConfigDefinition("build", THREAD_TYPE, LIST, DEFAULT_LIST, false,
                "Build list applied on each colony to decide which structures are ordered next."));
        configs.add(new ConfigDefinition("build", THREAD_TYPE, SWAP_TRANSPORT, "true", false,
                "Allow resource transport between colonies before building if the local colony is short on materials."));
        configs.add(new ConfigDefinition("build", THREAD_TYPE, SOURCE, CoordinateExpressionService.PLANETS, false,
                "Colony coordinate expression that selects where to build."));
        configs.add(new ConfigDefinition("build", THREAD_TYPE, BuildDefinition.LEAVE_PLANET_RESOURCES, null, false,
                "Resources to leave on each planet when calculating builds and resource transports."));
        configs.add(new ConfigDefinition("build", THREAD_TYPE, BuildDefinition.LEAVE_MOON_RESOURCES, null, false,
                "Resources to leave on each moon when calculating builds and resource transports."));
        return new ThreadDefinition(THREAD_TYPE,
                "Builds structures from a configured colony build sequence and resource strategy.",
                configs);
    }
}
