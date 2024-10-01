package org.enoch.snark.instance.si.module;

public class CleanPlanetsModule extends PropertiesModule{

    public static final String NAME = "clean_module";
    private static final String PROPERTIES =
            """
                    move_ships_to_moon.time=on
                    move_ships_to_moon.source=planet
                    move_ships_to_moon.leave_ships_wave=transporterLarge:1000,explorer:2,battleship:1
                    
                    move_resources_to_moon.time=on
                    move_resources_to_moon.source=planet
                    move_resources_to_moon.mission=transport
                    move_resources_to_moon.condition_resources_count_in_source=2m
                    move_resources_to_moon.resources=all
                    
                    move_ships_to_planet.time=on
                    move_ships_to_planet.ships_wave=transporterLarge:1000,explorer:2
                    move_ships_to_planet.expired_time=20:00""";

    public CleanPlanetsModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
