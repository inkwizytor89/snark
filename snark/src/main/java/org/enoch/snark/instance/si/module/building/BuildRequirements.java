package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.instance.model.to.Resources;

public class BuildRequirements {

    public BuildRequest request;
    public Resources resources;

    BuildRequirements(BuildRequest request, Resources resources) {
        this.request = request;
        this.resources = resources;
    }

    public boolean isResourceUnknown() {
        return Resources.unknown.equals(resources);
    }

    @Override
    public String toString() {
        return request + " (with " + resources + ")";
    }
}
