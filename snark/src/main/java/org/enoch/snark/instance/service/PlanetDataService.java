package org.enoch.snark.instance.service;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class PlanetDataService {

    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;

    public List<PlanetData> fetchCoordinates(String coordinates) {
        return fetchCoordinates(Planet.fromString(coordinates));
    }
    public List<PlanetData> fetchCoordinates(List<Planet> coordinates) {
        List<ColonyEntity> allColonies = colonyRepository.findAll();
        List<PlanetData> result = new ArrayList<>();
        for (Planet coordinate : coordinates) {
            Optional<ColonyEntity> optionalColony = allColonies.stream().filter(colony -> colony.toPlanet().equals(coordinate)).findFirst();
            if(optionalColony.isPresent()) result.add(new PlanetData(optionalColony.get()));
            else {
                Optional<TargetEntity> targetEntity = targetRepository.find(coordinate);
                result.add(targetEntity.map(PlanetData::new).orElseGet(() -> new PlanetData(new TargetEntity(coordinate))));
            }
        }
        return result;
    }

    public PlanetData fetchPlanetData(Planet planet) {
        if(planet.position ==16) {
            return new PlanetData(new TargetEntity(planet));
        }
        Optional<TargetEntity> targetEntity = targetRepository.find(planet);
        return targetEntity.map(PlanetData::new).orElseGet(() -> new PlanetData(colonyRepository.byPlanet(planet)));
    }

    public List<PlanetData> allColonies() {
        return colonyRepository.findAll().stream()
                .map(PlanetData::new)
                .toList();
    }

    public List<PlanetData> farms(Planet source, List<Planet> others) {
        return targetRepository.findFarmsCloserTo(source, others).stream().map(PlanetData::new).toList();
    }
}
