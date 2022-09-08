package org.enoch.snark.instance;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.Universe;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_RESEARCH;

public class Instance {

    protected static final Logger LOG = Logger.getLogger( Instance.class.getName());

    private static Instance INSTANCE;
    private static String serverConfigPath = "server.properties";
    private static Boolean isQueueEnabled = true;
    public static Universe universe;
    public static Commander commander;
    public static GI gi;
    public static GISession session;
    public static Integer level = 1;
    //    public MessageService messageService;
//    public List<ColonyEntity> sources;

    public LocalDateTime instanceStart = LocalDateTime.now();

    private Instance() {
        LOG.info("Config file " + serverConfigPath);
        loadServerProperties();
    }

    public static Instance getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Instance();
        }
        return INSTANCE;
    }

    public static void setServerProperties(String serverConfigPath) {
        Instance.serverConfigPath = serverConfigPath;
    }

    public synchronized void loadServerProperties() {
        try {
            universe = Universe.loadPrperties(new AppProperties(serverConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<ColonyEntity> loadGameState() {
        List<ColonyEntity> colonies = new ArrayList<>();
        try {
            PlayerEntity player = PlayerEntity.mainPlayer();
            PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(player);
            if(mainPlayer == null) {
                mainPlayer = player;
            }
            if(mainPlayer.level == null) {
                new GIUrlBuilder().openWithPlayerInfo(PAGE_RESEARCH, mainPlayer);
                mainPlayer.level = 1L;
            }
            PlayerDAO.getInstance().saveOrUpdate(mainPlayer);

            for(ColonyEntity colony : gi.loadPlanetList()) {
                ColonyEntity colonyEntity = ColonyDAO.getInstance().find(colony.cp);
                if(colonyEntity == null) {
                    colonyEntity = colony;
                } else if(colony.cpm != null && colonyEntity.cpm == null){
                    colonyEntity.cpm = colony.cpm;
                }
                if(colonyEntity.level == null) {
                    gi.updateColony(colonyEntity);
                    colonyEntity.level = 1L;
                }
                ColonyDAO.getInstance().saveOrUpdate(colonyEntity);
                colonies.add(colonyEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return colonies;
    }

    public void browserReset() {
        if(session != null) {
            session.getWebDriver().quit();
        }
        gi = GI.getInstance();
        session = new GISession(this);
        instanceStart = LocalDateTime.now();
    }

    public void run() {
        browserReset();
        loadGameState();
        if(isQueueEnabled) {
            commander = new CommanderImpl();
        } else {
            commander = new DumbCommanderImpl();
        }
        new BaseSI(this).run();
    }

    public ColonyEntity findNearestSource(PlanetEntity planet) {
        return this.findNearestSource(planet.toPlanet());
    }

    public ColonyEntity findNearestSource(Planet planet) {

        List<ColonyEntity> sources = new ArrayList<>(ColonyDAO.getInstance().fetchAll());

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
        Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(target.galaxy, target.system, target.position);
        if(targetEntity.isPresent()) {
            FleetDAO.getInstance().fetchAll().stream()
                    .filter(fleetEntity -> fleetEntity.id.equals(targetEntity.get().id))
                    .forEach(fleetEntity -> FleetDAO.getInstance().remove(fleetEntity));
            TargetDAO.getInstance().remove(targetEntity.get());
            //TODO: remove messegas and others
        }
    }

    public ColonyEntity getMainColony() {
        return ColonyDAO.getInstance().fetchAll().get(0);
    }

    public Long calcutateExpeditionSize() {
        return 2500L;
    }

    public synchronized boolean isStopped() {
        loadServerProperties();
        return universe.mode!= null && universe.mode.contains("stop");
    }

    public String getName() {
        return universe.name;
    }
}
