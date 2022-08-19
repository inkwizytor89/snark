package org.enoch.snark.instance;

import com.google.common.collect.ImmutableList;
import org.enoch.snark.db.DAOFactory;
import org.enoch.snark.db.entity.*;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.model.Planet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Instance {

    public UniverseEntity universeEntity;
    public Commander commander;
    public GI gi;
    public GISession session;
//    public MessageService messageService;
    public DAOFactory daoFactory;
    public ImmutableList<ColonyEntity> sources;

    public LocalDateTime instanceStart = LocalDateTime.now();

    public Instance(Long universeId) {
        this(universeId, true);
    }

    public Instance(Long universeId, boolean isQueueEnabled) {
        daoFactory = new DAOFactory(universeId);
        this.universeEntity = daoFactory.universeEntity;
        sources = ImmutableList.copyOf(universeEntity.colonyEntities);
        browserReset();
        if(isQueueEnabled) {
            commander = new CommanderImpl(this);
        } else {
            commander = new DumbCommanderImpl();
        }
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

        List<ColonyEntity> sources = daoFactory.colonyDAO.fetchAll().stream()
                .filter(colony -> universeEntity.id.equals(colony.universe.id))
                .collect(Collectors.toList());

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
        return 1900L;
    }

    public boolean isStopped() {
        daoFactory.entityManager.refresh(universeEntity);
        Optional<UniverseEntity> entity = daoFactory.universeDAO.fetchAllUniverses().stream()
                .filter(uni -> this.universeEntity.name.equals(uni.name))
                .findAny();
        String mode = entity.get().mode;
        return mode!= null && mode.contains("stop");
    }
}
