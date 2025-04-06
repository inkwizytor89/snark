package org.enoch.snark.instance;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.LoadColoniesCommand;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.action.command.UpdateResearchCommand;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.si.module.consumer.gi.GISession;
import org.enoch.snark.instance.service.Cleaner;
import org.enoch.snark.instance.si.module.consumer.ConsumerThread;
import org.enoch.snark.instance.config.ConfigReader;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.instance.si.module.PropertiesMap;
import org.enoch.snark.instance.si.module.ModuleMap;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.enoch.snark.instance.si.module.ThreadMap.GLOBAL;
import static org.enoch.snark.instance.si.module.ThreadMap.MAIN;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;

    @Getter
    private static PropertiesMap propertiesMap = new PropertiesMap();
    public static ConsumerThread consumerThread;
    public static Integer level = 1;

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

    public void run() {
        MessageService.getInstance();

        initialActionOnStart();
//        Core.getInstance();
        Cleaner.getInstance();
    }

    public void initialActionOnStart() {
//        consumer = Consumer.getInstance();
//        new ReadMessageCommand().hash(MessageService.class.getName()).push();
        //QueueManger.BUILDING
//        new OpenPageCommand(LFBUILDINGS, ColonyDAO.getInstance().find("p[2:447:8]")).push();
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

    public static ThreadMap getGlobalMainConfigMap() {
        return getGlobalMainConfigMap(MAIN);
    }

    public static ThreadMap getGlobalMainConfigMap(String name) {
        throw new NotImplementedException("getGlobalMainConfigMap not to use");
//        ModuleMap moduleMap = propertiesMap.get(GLOBAL);
//        if(!moduleMap.containsKey(name)) return new ThreadMap();
//        return moduleMap.get(name);
    }

    public static List<ColonyEntity> getSources() {
        return getGlobalMainConfigMap().getSources();
    }
}
