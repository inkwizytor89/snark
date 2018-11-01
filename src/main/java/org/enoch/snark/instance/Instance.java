package org.enoch.snark.instance;

import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.macro.MessageService;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SourcePlanet;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.module.explore.SpaceModule;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Instance {

    public UniverseEntity universeEntity;
    public Commander commander;
    public GISession session;
    public MessageService messageService;

    public Instance(UniverseEntity universeEntity) {
        this(universeEntity, true);
    }

    public Instance(UniverseEntity universeEntity, boolean isQueueEnabled) {
        this.universeEntity = universeEntity;
        session = new GISession(this);
        if(isQueueEnabled) {
            commander = new CommanderImpl(this);
        } else {
            commander = new DumbCommanderImpl();
        }

        new GIUrlBuilder(this).updateFleetStatus();
    }

    public void runTest() {
//        new GalaxyAnalyzeCommand(this, new SystemView(2, 109)).execute();
        final SpaceModule spaceModule = new SpaceModule(this);
        while(true) spaceModule.run();
    }

    public void runSI() {


        new SI(this).run();
    }

    public SourcePlanet findNearestSource(Planet planet) {
        throw new NotImplementedException();
//        SourcePlanet nearestPlanet = appProperties.sourcePlanets.get(0);
//        Integer minDistance = planet.calculateDistance(nearestPlanet);
//
//        for(SourcePlanet source : appProperties.sourcePlanets) {
//            Integer distance = planet.calculateDistance(source);
//            if (distance < minDistance) {
//                minDistance = distance;
//                nearestPlanet = source;
//            }
//        }
//        return nearestPlanet;
    }
}
