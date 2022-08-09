package org.enoch.snark.instance;

import com.google.common.collect.ImmutableList;
import org.enoch.snark.db.DAOFactory;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.Planet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Instance {

    public UniverseEntity universeEntity;
    public Commander commander;
    public GI gi;
    public GISession session;
//    public MessageService messageService;
    public DAOFactory daoFactory;
    public ImmutableList<ColonyEntity> sources;

    public LocalDateTime instanceStart = LocalDateTime.now();

    public Instance(UniverseEntity universeEntity) {
        this(universeEntity, true);
    }

    public Instance(UniverseEntity universeEntity, boolean isQueueEnabled) {
        this.universeEntity = universeEntity;
        sources = ImmutableList.copyOf(universeEntity.colonyEntities);
        daoFactory = new DAOFactory(universeEntity);
        browserReset();
        if(isQueueEnabled) {
            commander = new CommanderImpl(this);
        } else {
            commander = new DumbCommanderImpl();
        }
        new GIUrlBuilder(this).updateFleetStatus();
    }

    public void browserReset() {
        if(session != null) {
            session.getWebDriver().quit();
        }
        gi = new GI();
        session = new GISession(this);
        instanceStart = LocalDateTime.now();
    }

    public void runTest() {
    }

    public void runSI() {
        new BaseSI(this).run();
    }

    public ColonyEntity findNearestSource(PlanetEntity planet) {
        return this.findNearestSource(planet.toPlanet());
    }

    public ColonyEntity findNearestSource(Planet planet) {

        List<ColonyEntity> sources = daoFactory.colonyDAO.fetchAll();

        ColonyEntity nearestPlanet = sources.get(0);
        Integer minDistance = planet.calculateDistance(sources.get(0).toPlanet());

        for(ColonyEntity source : sources) {
            Integer distance = planet.calculateDistance(source.toPlanet());
            if (distance < minDistance) {
                minDistance = distance;
                nearestPlanet = source;
            }
        }
        return nearestPlanet;
    }

    public void removePlanet(Planet target) {
        Optional<TargetEntity> targetEntity = daoFactory.targetDAO.find(target.galaxy, target.system, target.position);
        if(targetEntity.isPresent()) {
            daoFactory.fleetDAO.fetchAll().stream()
                    .filter(fleetEntity -> fleetEntity.id.equals(targetEntity.get().id))
                    .forEach(fleetEntity -> daoFactory.fleetDAO.remove(fleetEntity));
            daoFactory.targetDAO.remove(targetEntity.get());
            //TODO: remove messegas and others
        }
    }

    public Long calcutateExpeditionSize() {
        return 1200L;
    }
}
