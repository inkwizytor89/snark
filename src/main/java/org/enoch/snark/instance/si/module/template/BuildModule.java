package org.enoch.snark.instance.si.module.template;

import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesModule;

public class BuildModule extends PropertiesModule {

    public static final String NAME = "build_module";
    private static final String PROPERTIES =
            """
                    build_planet.time=off
                    build_planet.list=base
                    build_forms.source=planet
            
                    build_moon.time=off
                    build_moon.source=moon
                    build_moon.list=fasteleport
            
                    build_forms.time=off
                    build_forms.list=mechat2
                    build_forms.source=planet
            """;

    public BuildModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected String getProperties() {
        return PROPERTIES;
    }
}
