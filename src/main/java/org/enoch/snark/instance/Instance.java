package org.enoch.snark.instance;

import org.enoch.snark.db.DAOFactory;
import org.enoch.snark.db.entity.*;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.Universe;
import org.enoch.snark.module.AbstractModule;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Instance {

    protected static final Logger LOG = Logger.getLogger( Instance.class.getName());

    public String serverConfigPath;
    public Universe universe;
    public Commander commander;
    public GI gi;
    public GISession session;
//    public MessageService messageService;
    public DAOFactory daoFactory;
    public List<ColonyEntity> sources;

    public LocalDateTime instanceStart = LocalDateTime.now();

    public Instance(String serverConfigPath) {
        this(serverConfigPath, true);
    }

    public Instance(String serverConfigPath, boolean isQueueEnabled) {
        this.serverConfigPath = serverConfigPath;
        LOG.info("Config file "+this.serverConfigPath);
        this.universe = loadServerProperties(this.serverConfigPath);
        daoFactory = new DAOFactory();
        browserReset();
        sources = loadGameState();
//        sources = new ArrayList<>(ImmutableList.copyOf(universeEntity.colonyEntities));
        if(isQueueEnabled) {
            commander = new CommanderImpl(this);
        } else {
            commander = new DumbCommanderImpl();
        }
    }

    public Universe loadServerProperties(String serverConfigPath) {
        AppProperties appProperties = null;
        try {
            appProperties = new AppProperties(serverConfigPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Universe.loadPrperties(appProperties);
    }

    public List<ColonyEntity> loadGameState() {
        List<ColonyEntity> colonies = new ArrayList<>();
        try {
            for(ColonyEntity colony : gi.loadPlanetList()) {
                ColonyEntity colonyEntity = daoFactory.colonyDAO.find(colony.cp);
                if(colonyEntity == null) {
                    colonyEntity = colony;
                } else if(colony.cpm != null && colonyEntity.cpm == null){
                    colonyEntity.cpm = colony.cpm;
                }
                gi.updateColony(colonyEntity);
                daoFactory.colonyDAO.saveOrUpdate(colonyEntity);
                colonies.add(colonyEntity);
            }
            gi.updateResearch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return colonies;
    }

    public void browserReset() {
        if(session != null) {
            session.getWebDriver().quit();
        }
        gi = new GI();
        session = new GISession(this);
        instanceStart = LocalDateTime.now();
    }

    public void runSI() {
        new BaseSI(this).run();
    }

    public ColonyEntity findNearestSource(PlanetEntity planet) {
        return this.findNearestSource(planet.toPlanet());
    }

    public ColonyEntity findNearestSource(Planet planet) {

        List<ColonyEntity> sources = new ArrayList<>(daoFactory.colonyDAO.fetchAll());

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
        return 2500L;
    }

    public synchronized boolean isStopped() {
        this.universe = this.loadServerProperties(this.serverConfigPath);
        return this.universe.mode!= null && this.universe.mode.contains("stop");
    }

    public String getName() {
        return universe.name;
    }
}
