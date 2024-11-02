package org.enoch.snark.instance;

import lombok.Getter;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.*;
import org.enoch.snark.instance.model.action.QueueManger;
import org.enoch.snark.instance.service.Cleaner;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.config.ConfigReader;
import org.enoch.snark.instance.si.BaseSI;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.module.ConfigMap;
import org.enoch.snark.instance.si.module.PropertiesMap;
import org.enoch.snark.instance.si.module.ModuleMap;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.gi.types.UrlComponent.*;
import static org.enoch.snark.instance.si.module.ConfigMap.GLOBAL;
import static org.enoch.snark.instance.si.module.ConfigMap.MAIN;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;

    @Getter
    private static PropertiesMap propertiesMap = new PropertiesMap();
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
//        new ReadMessageCommand().hash(MessageService.class.getName()).push();
        //QueueManger.BUILDING
        new OpenPageCommand(LFBUILDINGS, ColonyDAO.getInstance().find("p[3:122:8]")).push();
        new LoadColoniesCommand().push();
        new UpdateFleetEventsCommand().push();
        new UpdateResearchCommand().push();
        getSources().forEach(colony -> new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()).push());
    }

    public void removePlanet(Planet target) {
        Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(target);
        if(targetEntity.isPresent()) {
//            FleetDAO.getInstance().fetchAll().stream()
//                    .filter(fleetEntity -> fleetEntity.id.equals(targetEntity.get().id))
//                    .forEach(fleetEntity -> FleetDAO.getInstance().remove(fleetEntity));
            TargetDAO.getInstance().remove(targetEntity.get());
            LOG.info("Removed "+target);
            //TODO: remove messegas and others
        }
    }

    public static ConfigMap getMainConfigMap() {
        return getMainConfigMap(MAIN);
    }

    public static ConfigMap getMainConfigMap(String name) {
        ModuleMap moduleMap = propertiesMap.get(GLOBAL);
        if(!moduleMap.containsKey(name)) return new ConfigMap();
        return moduleMap.get(name);
    }

    public static List<ColonyEntity> getSources() {
        return getMainConfigMap().getSources();
    }
}
