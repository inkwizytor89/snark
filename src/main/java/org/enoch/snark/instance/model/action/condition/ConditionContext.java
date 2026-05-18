package org.enoch.snark.instance.model.action.condition;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.db.entity.CacheEntryEntity;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.service.PlanetDataService;
import org.enoch.snark.instance.service.ShipService;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.model.to.Planet.*;

@Component
@RequiredArgsConstructor
@Data
public class ConditionContext {

    private final PlanetDataService planetDataService;
    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
    private final FleetRepository fleetRepository;
    private final TargetRepository targetRepository;
    private final CoordinateExpressionService coordinateExpressionService;
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

    @Deprecated // use planetData
    public PlanetEntity planet(Planet planet) {
        if(SOURCE_TERM.equals(planet)) return command.getSource();
        else if(TARGET_TERM.equals(planet))  return command.getTarget().planetData();
        else return planetDataService.fetchPlanetData(planet).planetData();
    }

    public PlanetData planetData(Planet planet) {
        if(SOURCE_TERM.equals(planet)) return new PlanetData(command.getSource());
        else if(TARGET_TERM.equals(planet))  return command.getTarget();
        else return planetDataService.fetchPlanetData(planet);
    }

    public String getCacheValue(String key) {
        return cacheEntryRepository.getValue(key);
    }

    public CacheEntryEntity getCacheEntry(String key) {
        return cacheEntryRepository.findByKey(key).orElseThrow(() -> new IllegalStateException("Missing CacheEntry wih key "+key));
    }
}
