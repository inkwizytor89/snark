package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.instance.model.technology.Technology;

public class BuildRequest {

    public Technology technology;
    public Long level;

    public BuildRequest(Technology technology, long level) {
        this.technology = technology;
        this.level = level;
    }

    @Override
    public String toString() {
        return technology.name() + " " + level;
    }
}
