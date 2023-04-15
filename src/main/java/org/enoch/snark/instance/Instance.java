package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.RefreshColoniesStateCommand;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.instance.config.Universe;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.service.MessageService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_RESEARCH;
import static org.enoch.snark.instance.config.Config.MODE;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;
    public static Config config;
    public static Commander commander;
    public static GI gi;
    public static GISession session;
    public static Integer level = 1;
    private ColonyDAO colonyDAO;

    public List<Planet> cachedPlaned = new ArrayList<>();
    public List<ColonyEntity> flyPoints = new ArrayList<>();
    public ColonyEntity lastVisited = null;

    public LocalDateTime instanceStart = LocalDateTime.now();

    private Instance() {
        updateConfig();
    }

    public static Instance getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Instance();
        }
        return INSTANCE;
    }

    public static synchronized void updateConfig() {
        try {
            config = Universe.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ColonyEntity> getFlyPoints() {
        while(flyPoints.isEmpty()) {
            System.err.println("Waiting for FlyPoints");
            SleepUtil.sleep();
        }
        return flyPoints;
    }

    public void browserReset() {
        if(session != null) {
            session.getWebDriver().close();
            session.getWebDriver().quit();

        }
        GI.restartInstance();
        gi = GI.getInstance();
        session = new GISession(this);
        instanceStart = LocalDateTime.now();
    }

    public void run() {
        colonyDAO = ColonyDAO.getInstance();
        MessageService.getInstance();

        browserReset();
        initialActionOnStart();
        BaseSI.getInstance();
    }

    public void initialActionOnStart() {
        commander = Commander.getInstance();
        commander.pushFleet(new LoadColoniesCommand());
        PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
        commander.pushFleet(new OpenPageCommand(PAGE_RESEARCH, mainPlayer).setCheckEventFleet(true));
        commander.pushFleet(new RefreshColoniesStateCommand());
    }

    public void removePlanet(Planet target) {
        Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(target.galaxy, target.system, target.position);
        if(targetEntity.isPresent()) {
//            FleetDAO.getInstance().fetchAll().stream()
//                    .filter(fleetEntity -> fleetEntity.id.equals(targetEntity.get().id))
//                    .forEach(fleetEntity -> FleetDAO.getInstance().remove(fleetEntity));
            TargetDAO.getInstance().remove(targetEntity.get());
            LOG.info("Removed "+target);
            //TODO: remove messegas and others
        }
    }

    public ColonyEntity getMainColony() {
        return colonyDAO.fetchAll().get(0);
    }

    public synchronized boolean isStopped() {
        return config.getConfig(MODE)!= null && config.getConfig(MODE).contains("stop");
    }

    public void push(AbstractCommand command) {
        push(command, false);
    }

    public void push(AbstractCommand command, boolean shouldUseFleetActionQueue) {
        if(shouldUseFleetActionQueue) {
            commander.pushFleet(command);
        } else {
            commander.push(command);
        }
    }
}
