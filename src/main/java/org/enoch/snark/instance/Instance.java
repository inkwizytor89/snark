package org.enoch.snark.instance;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.UpdateFleetEventsCommand;
import org.enoch.snark.gi.command.impl.UpdateResearchCommand;
import org.enoch.snark.instance.service.Cleaner;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.config.ConfigReader;
import org.enoch.snark.instance.si.BaseSI;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.ConfigMap.GALAXY_MAX;
import static org.enoch.snark.instance.si.module.ConfigMap.TRIP;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;

    private static HashMap<String, ConfigMap> propertiesMap = new HashMap<>();
    public static Commander commander;
    public static GISession session;
    public static Integer level = 1;

    public ColonyEntity lastVisited = null;

    private Instance() {
        updatePropertiesMap();
    }

    public static Instance getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Instance();
        }
        return INSTANCE;
    }

    public static synchronized void updatePropertiesMap() {
        try {
            propertiesMap = ConfigReader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        new LoadColoniesCommand().push();
        new UpdateFleetEventsCommand().push();
        new UpdateResearchCommand().push();
        getSources().forEach(colony -> new OpenPageCommand(FLEETDISPATCH, colony).push());
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

    public static ConfigMap getConfigMap(String tag) {
        if(!propertiesMap.containsKey(tag)) return new ConfigMap();
        return propertiesMap.get(tag);
    }

    public static ConfigMap getMainConfigMap() {
        return propertiesMap.get(ConfigMap.MAIN);
    }

    public static List<ColonyEntity> getSources() {
        return getMainConfigMap().getSources();
    }

    public static HashMap<String, ConfigMap> getPropertiesMap() {
        return propertiesMap;
    }

    public static ColonyEntity next(ColonyEntity colonyEntity) {
        Integer galaxyCount = Instance.getMainConfigMap().getConfigInteger(GALAXY_MAX, 6);
        List<ColonyEntity> configTrip = Instance.getMainConfigMap().getColonies(TRIP, null);
        if(configTrip == null) throw new IllegalStateException("Missing config: "+TRIP);
        int index = configTrip.indexOf(colonyEntity);
        if(index == -1) {
            ColonyEntity swapColony = ColonyDAO.getInstance().get(colonyEntity.toPlanet().swapType());
            index = configTrip.indexOf(swapColony);
        }
        if(index==-1) return null;
        else return configTrip.get((index + 1) %  configTrip.size());
    }
}
