package org.enoch.snark.instance.si.module.template;

import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesModule;

public class SleepModule extends PropertiesModule {

    public static final String NAME = "sleep_module";
    private static final String PROPERTIES =
            """
                    sleep.time=on
                    sleep.source=moon
                    sleep.target=next
                    sleep.condition_ships_in_source=deathstar:1
                    sleep.condition_blocking_missions=TRANSPORT;ATTACK;EXPEDITION
                    sleep.resources=all
                    sleep.leave_resources=nothing
                    sleep.speed=30
            """;

    public SleepModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
