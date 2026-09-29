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

    public static boolean isFactory(Technology technology) {
        return technology != null && technology.name() != null && (
                Building.roboticsFactory.name().equals(technology.name()) ||
                Building.naniteFactory.name().equals(technology.name()));
    }

    public static boolean isLFLaboratory(Technology technology) {
        return technology != null && technology.name() != null && (
                LFBuilding.lifeformTech11103.name().equals(technology.name()) ||
                        LFBuilding.lifeformTech12103.name().equals(technology.name()) ||
                        LFBuilding.lifeformTech13103.name().equals(technology.name()) ||
                        LFBuilding.lifeformTech14103.name().equals(technology.name()));
    }

    public static boolean isLaboratory(Technology technology) {
        return technology != null && technology.name() != null && (
                Building.researchLaboratory.name().equals(technology.name()));
    }
}
