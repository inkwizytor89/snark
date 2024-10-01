package org.enoch.snark.instance.si.module;

public class Module extends AbstractModule{

    public Module(ModuleMap moduleMap) {
        super(moduleMap);
    }

    @Override
    protected ModuleMap createBaseMap() {
        return new ModuleMap();
    }
}
