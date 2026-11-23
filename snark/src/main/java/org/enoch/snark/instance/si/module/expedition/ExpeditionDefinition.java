package org.enoch.snark.instance.si.module.expedition;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.ThreadConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExpeditionDefinition implements ThreadConfig {

    public static final String THREAD_TYPE = "expedition";
    public static final String SOURCE = "source";
    public static final String MAX_DT = "max_dt";
    public static final String BATTLE_EXTENSION = "battle_extension";
    public static final String SYSTEM_SHIFT = "system_shift";
    public static final String SHIPS = "ships";
    public static final String DEFAULT_SHIPS_VALUE = "explorer:1,transporterLarge:2500,battleship:1";

    private String source;
    private Integer maxDt;
    private Boolean battleExtension;
    private Integer systemShift;
    private ShipsMap ships;

    public ExpeditionDefinition() {
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
            case MAX_DT -> {
                if (value != null && !value.isBlank()) maxDt = Integer.valueOf(value);
            }
            case BATTLE_EXTENSION -> {
                if (value != null && !value.isBlank()) battleExtension = Boolean.valueOf(value);
            }
            case SYSTEM_SHIFT -> {
                if (value != null && !value.isBlank()) systemShift = Integer.valueOf(value);
            }
            case SHIPS -> {
                if (value != null && !value.isBlank()) ships = ShipsMap.parse(value);
            }
            default -> {
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("expedition", THREAD_TYPE, SOURCE, "", false,
                "Source colony set used for expedition planning."));
        configs.add(new ConfigDefinition("expedition", THREAD_TYPE, MAX_DT, "2500", false,
                "Maximum transport capacity used for expedition ships."));
        configs.add(new ConfigDefinition("expedition", THREAD_TYPE, BATTLE_EXTENSION, "true", false,
                "Whether to include battle ships in expedition groups."));
        configs.add(new ConfigDefinition("expedition", THREAD_TYPE, SYSTEM_SHIFT, "0", false,
                "System offset applied to the expedition target."));
        configs.add(new ConfigDefinition("expedition", THREAD_TYPE, SHIPS, DEFAULT_SHIPS_VALUE, false,
                "Ships required for each expedition when enough ships are available."));
        return new ThreadDefinition(THREAD_TYPE,
                "Sends expeditions to distant systems and balances transport capability.",
                configs);
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = switch (config.getKey()) {
                case SOURCE -> source;
                case MAX_DT -> maxDt == null ? null : maxDt.toString();
                case BATTLE_EXTENSION -> battleExtension == null ? null : battleExtension.toString();
                case SYSTEM_SHIFT -> systemShift == null ? null : systemShift.toString();
                case SHIPS -> formatShips(ships);
                default -> config.getValue();
            };
            configs.add(new ConfigDefinition(
                    config.getModule(),
                    config.getThread(),
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        return new ThreadDefinition(definition.getThread(), definition.getDescription(), configs);
    }

    private String formatShips(ShipsMap value) {
        if (value == null) {
            return null;
        }
        if (value == ShipsMap.ALL_SHIPS) {
            return "all";
        }
        if (value == ShipsMap.NO_SHIPS) {
            return "none";
        }
        return value.entrySet().stream()
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
    }
}
