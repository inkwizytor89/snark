package org.enoch.snark.instance.si.module.template;

import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesModule;

public class TestModule extends PropertiesModule {

    public static final String NAME = "test_module";
    private static final String PROPERTIES =
            """
                    move.time=on
                    move.condition_resources_count_in_source=2m
                    move.mission=transport
                    move.ships_wave=colonyShip:1
                    move.resources=m1
                    move.expired_time=02:00
                    
            """;

    public TestModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
