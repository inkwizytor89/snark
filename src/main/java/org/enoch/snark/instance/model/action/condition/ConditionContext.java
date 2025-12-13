package org.enoch.snark.instance.model.action.condition;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.service.ShipService;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.model.to.Planet.*;

@Component
@RequiredArgsConstructor
@Data
public class ConditionContext {
    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final FleetRepository fleetRepository;
    private final TargetRepository targetRepository;
    private final PlanetService planetService;
    private final ShipService shipService;

    private SendCommand command;

    public ColonyEntity colony(Planet planet) {
        if(SOURCE_TERM.equals(planet)) return command.getSource();
        if(TARGET_TERM.equals(planet)) return command.getTarget().getColony();
        return getColonyRepository().byPlanet(planet);
    }

    public TargetEntity target(Planet planet) {
        if(SOURCE_TERM.equals(planet)) throw new IllegalStateException("Incorrect "+SOURCE_STRING);
        if(TARGET_TERM.equals(planet)) return command.getTarget().getTarget();
        return getTargetRepository().byPlanet(planet);
    }

    public PlanetEntity planet(Planet planet) {
        if(SOURCE_TERM.equals(planet)) return command.getSource();
        else if(TARGET_TERM.equals(planet))  return command.getTarget().planetData();
        else return determinePlanetEntity(planet);
    }

    private PlanetEntity determinePlanetEntity(Planet planet) {
        return planetService.getPlanetData(planet).planetData();
    }

    public String getCacheValue(String key) {
        return cacheEntryRepository.getValue(key);
    }
}
