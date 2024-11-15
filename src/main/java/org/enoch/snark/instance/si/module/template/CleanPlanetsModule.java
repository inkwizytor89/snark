package org.enoch.snark.instance.si.module.template;

import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesModule;

public class CleanPlanetsModule extends PropertiesModule {

    public static final String NAME = "clean_module";
    private static final String PROPERTIES =
            """
                    move_ships_to_moon.time=09:40?8M-09:59;10:40?8M-10:59;14:40?8M-14:59;17:40?8M-17:59;18:40?8M-18:59
                    move_ships_to_moon.source=planet
                    move_ships_to_moon.leave_ships_wave=transporterLarge:1000,explorer:2,battleship:1
             
                    move_resources_to_moon.time=09:20?8M-09:39;10:20?8M-10:39;14:20?8M-14:39;17:20?8M-17:39;18:20?8M-18:39
                    move_resources_to_moon.source=planet
                    move_resources_to_moon.mission=transport
                    move_resources_to_moon.condition_resources_count_in_source=4m
                    move_resources_to_moon.resources=all
             
                    move_ships_to_planet.time=09:00?8M-09:19;10:00?8M-10:19;14:00?8M-14:19;17:00?8M-17:19;18:00?8M-18:19
                    move_ships_to_planet.condition_ships_in_source=transporterLarge:100,explorer:1
                    move_ships_to_planet.ships_wave=transporterLarge:1000,explorer:2
                    move_ships_to_planet.expired_time=20M
             """;

    public CleanPlanetsModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
