package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.Resources;

public class BuildRequirements {

    public BuildingRequest request;
    public Resources resources;

    BuildRequirements(BuildingRequest request, Resources resources) {
        this.request = request;
        this.resources = resources;
    }

    public boolean canBuildOn(ColonyEntity colony) {
        if(colony == null) return false;
        return colony.hasEnoughResources(resources);
    }

    public boolean isResourceUnknown() {
        return Resources.unknown.equals(resources);
    }

    @Override
    public String toString() {
        return request + " (with " + resources + ")";
    }
}
