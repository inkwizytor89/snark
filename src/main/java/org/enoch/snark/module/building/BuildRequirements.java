package org.enoch.snark.module.building;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.model.Resources;

public class BuildRequirements {

    public BuildingRequest request;
    public Resources resources;

    BuildRequirements(BuildingRequest request, Resources resources) {
        this.request = request;
        this.resources = resources;
    }

    public boolean canBuildOn(ColonyEntity colony) {
        return colony.metal > resources.metal && colony.crystal > resources.crystal && colony.deuterium > resources.deuterium;
    }

    public boolean isResourceUnknown() {
        return Resources.unknown.equals(resources);
    }

    @Override
    public String toString() {
        return request + "(with " + resources + ")";
    }
}
