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
import org.enoch.snark.gi.command.impl.AbstractCommand;
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

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;
    private static String serverConfigPath = "server.properties";
    public static Universe universe;
    public static Commander commander;
    public static GI gi;
    public static GISession session;
    public static Integer level = 1;

    public List<Planet> cachedPlaned = new ArrayList<>();
    public ColonyEntity lastVisited = null;
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
    public void loadGameState() {
        try {
            PlayerEntity player = PlayerEntity.mainPlayer();
            PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(player);
            if(mainPlayer == null) {
                mainPlayer = player;
            }

            // init database with colonies to not get NPE
            ColonyDAO colonyDAO = ColonyDAO.getInstance();
            for(ColonyEntity colony : gi.loadPlanetList()) {
                ColonyEntity colonyEntity = colonyDAO.find(colony.cp);
                if (colonyEntity == null) {
                    colonyEntity = colony;
                } else if (colony.cpm != null && colonyEntity.cpm == null) {
                    colonyEntity.cpm = colony.cpm;
                }
                colonyDAO.saveOrUpdate(colonyEntity);
            }

            // update colonies
            for(ColonyEntity colony : colonyDAO.fetchAll()) {
                if(colony.isPlanet) {
                    cachedPlaned.add(colony.toPlanet());
                }
                if(colony.level == null) {
                    gi.updateColony(colony);
                    colony.level = 1L;
                }
                colonyDAO.saveOrUpdate(colony);

                if(mainPlayer.level == null) {
                    new GIUrlBuilder().openWithPlayerInfo(PAGE_RESEARCH, mainPlayer);
                    mainPlayer.level = 1L;
                }
                PlayerDAO.getInstance().saveOrUpdate(mainPlayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void browserReset() {
        if(session != null) {
            session.getWebDriver().quit();
        }
        GI.restartInstance();
        gi = GI.getInstance();
        session = new GISession(this);
        instanceStart = LocalDateTime.now();
    }

    public void run() {
        browserReset();
        loadGameState();
        LOG.info("loading game state successful");
        commander = new Commander();
        LOG.info("Commander start successful");
        new BaseSI(this).run();
        LOG.info("SI start successful");
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
            LOG.info("Removed "+target);
            //TODO: remove messegas and others
        }
    }

    public ColonyEntity getMainColony() {
        return ColonyDAO.getInstance().fetchAll().get(0);
    }

    public Long calculateMinExpeditionSize() {
        String minDt = this.universe.getConfig((Universe.MIN_DT));
        if(minDt == null || minDt.isEmpty()) {
            return 1700L;
        }
        return Long.parseLong(minDt);
    }

    public Long calculateMaxExpeditionSize() {
        String maxDt = this.universe.getConfig((Universe.MAX_DT));
        if(maxDt == null || maxDt.isEmpty()) {
            return 2500L;
        }
        return Long.parseLong(maxDt);
    }

    public synchronized boolean isStopped() {
        loadServerProperties();
        return universe.mode!= null && universe.mode.contains("stop");
    }

    public void push(AbstractCommand command) {
        commander.push(command);
    }
}
