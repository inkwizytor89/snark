package org.enoch.snark.instance.si.module.template;

import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesModule;

public class DutyModule extends PropertiesModule {

    public static final String NAME = "duty_module";
    private static final String PROPERTIES =
            """
                    prepare.time=07:00-20:00
                    prepare.source=moon
                    prepare.condition_ships_in_source=deathstar:6
                    prepare.leave_ships_wave=transporterLarge:15000,explorer:6,battleship:1,espionageProbe:800,colonyShip:all,deathstar:2
                    prepare.leave_resources=m11m c21m d32kk
                    prepare.resources=all
                    prepare.queue=MAJOR
                    prepare.debug=false
            
                    standby.time=07:00-20:30
                    standby.source=planet
                    standby.condition_ships_in_source=deathstar:6
                    standby.leave_ships_wave=transporterLarge:2000,explorer:2,battleship:1
                    standby.resources=all
                    standby.speed=10
                    standby.recall=10M-12M
                    standby.queue=MAJOR
                    standby.debug=false
            
                    finish.time=20:30-23:59
                    finish.source=planet
                    finish.condition_ships_in_source=deathstar:6
                    finish.leave_ships_wave=transporterLarge:15000,explorer:6,battleship:1,espionageProbe:800,colonyShip:all,deathstar:2
                    finish.resources=all
                    on_standby.speed=10
                    finish.queue=MAJOR
                    finish.debug=false
            """;

    public DutyModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
