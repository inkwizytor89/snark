package org.enoch.snark.instance.si.module.building.list.planet;

import org.enoch.snark.instance.model.technology.Building;
import org.enoch.snark.instance.model.technology.Defense;
import org.enoch.snark.instance.model.technology.LFBuilding;
import org.enoch.snark.instance.model.technology.LFResearch;
import org.enoch.snark.instance.model.technology.Research;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.technology.Technology;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.List;

import static java.util.Collections.singletonList;

public class Custom extends AbstractBuildingList {
    public static final String code = Custom.class.getSimpleName().toLowerCase();

    public Custom(String code) {
        super(code);
    }

    public List<BuildRequest> create() {
        Technology technology = findTechnology();
        return singletonList(new BuildRequest(technology, argLong(1)));
    }

    private Technology findTechnology() {
        Technology technology = find(Building.values());
        if (technology == null) technology = find(Research.values());
        if (technology == null) technology = find(Defense.values());
        if (technology == null) technology = find(LFBuilding.values());
        if (technology == null) technology = find(LFResearch.values());
        if (technology == null) technology = find(Ship.values());
        if (technology == null) {
            throw new IllegalArgumentException("Unknown technology: " + type);
        }
        return technology;
    }

    private <T extends Enum<T> & Technology> T find(T[] technologies) {
        for (T technology : technologies) {
            if (technology.name().equalsIgnoreCase(type)
                    || (technology.getDeclaringClass().getSimpleName() + "." + technology.name())
                    .equalsIgnoreCase(type)) {
                return technology;
            }
        }
        return null;
    }
}
