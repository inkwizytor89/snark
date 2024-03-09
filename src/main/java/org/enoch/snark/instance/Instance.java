package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.UpdateFleetEventsCommand;
import org.enoch.snark.gi.command.impl.UpdateResearchCommand;
import org.enoch.snark.instance.commander.Cleaner;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.instance.config.Universe;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.service.MessageService;
import org.enoch.snark.module.ConfigMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.gi.macro.UrlComponent.FLEETDISPATCH;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;
    public static Config config;
    public static Commander commander;
//    public static GI gi;
    public static GISession session;
    public static Integer level = 1;

    public List<Planet> cachedPlaned = new ArrayList<>();
    public List<ColonyEntity> flyPoints = new ArrayList<>();
    public ColonyEntity lastVisited = null;

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

    public void startBrowser() {
        session = GISession.getInstance();
    }

    public void run() {
        MessageService.getInstance();

        startBrowser();
        initialActionOnStart();
        BaseSI.getInstance();
        Cleaner.getInstance();
    }

    public void initialActionOnStart() {
        commander = Commander.getInstance();
        // LoadColoniesCommand to refactor
        new LoadColoniesCommand().push();
        new UpdateFleetEventsCommand().push();
        new UpdateResearchCommand().push();
        getMainConfigMap().getFlyPoints().forEach(colony -> new OpenPageCommand(FLEETDISPATCH, colony).push());
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
//
//    public synchronized boolean isStopped() {
//        return config.getConfig(MODE)!= null && config.getConfig(MODE).contains("stop");
//    }

    public static String getGlobalConfig(String tag, String key) {
        if(!config.globalMap.containsKey(tag)) return null;
        ConfigMap configMap = config.globalMap.get(tag);
        if(!configMap.containsKey(tag)) return null;
        return configMap.get(key);
    }

    public static ConfigMap getConfigMap(String tag) {
        if(!config.globalMap.containsKey(tag)) return new ConfigMap();
        return config.globalMap.get(tag);
    }

    public static ConfigMap getMainConfigMap() {
        return config.globalMap.get(ConfigMap.MAIN);
    }
}
