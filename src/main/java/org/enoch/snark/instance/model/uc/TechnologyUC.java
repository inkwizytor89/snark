package org.enoch.snark.instance.model.uc;

import org.enoch.snark.instance.model.technology.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TechnologyUC {

    public static Technology parse(Long id) {
        Optional<Technology> optionalTechnology = allTechnologies().stream()
                .filter(technology -> technology.getId().equals(id))
                .findFirst();
        return optionalTechnology.orElseThrow(() -> new IllegalStateException("Unknown technology with id "+id));
    }

    public static List<Technology> allTechnologies() {
        List<Technology> technologies = new ArrayList<>();
        technologies.addAll(Arrays.asList(Building.values()));
        technologies.addAll(Arrays.asList(LFBuilding.values()));
        technologies.addAll(Arrays.asList(Research.values()));
        technologies.addAll(Arrays.asList(Ship.values()));
        technologies.addAll(Arrays.asList(Defense.values()));
        return technologies;
    }
}
