package org.enoch.snark.instance;

import com.google.common.collect.ImmutableList;
import org.enoch.snark.db.DAOFactory;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.SourceEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.GalaxyAnalyzeCommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.SystemView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Instance {

    public UniverseEntity universeEntity;
    public Commander commander;
    public GISession session;
//    public MessageService messageService;
    public DAOFactory daoFactory;
    public ImmutableList<SourceEntity> sources;

    public Instance(UniverseEntity universeEntity) {
        this(universeEntity, true);
    }

    public Instance(UniverseEntity universeEntity, boolean isQueueEnabled) {
        this.universeEntity = universeEntity;
        sources = ImmutableList.copyOf(universeEntity.sources);
        daoFactory = new DAOFactory(universeEntity);
        session = new GISession(this);
        if(isQueueEnabled) {
            commander = new CommanderImpl(this);
        } else {
            commander = new DumbCommanderImpl();
        }
        new GIUrlBuilder(this).updateFleetStatus();
    }

    public void runTest() {
    }

    public void runSI() {
        new ResourceSI(this).run();
    }

    public SourceEntity findNearestSource(PlanetEntity planet) {

        List<SourceEntity> sources = daoFactory.sourceDAO.fetchAll();

        SourceEntity nearestPlanet = sources.get(0);
        Integer minDistance = planet.calculateDistance(sources.get(0));

        for(SourceEntity source : sources) {
            Integer distance = planet.calculateDistance(source);
            if (distance < minDistance) {
                minDistance = distance;
                nearestPlanet = source;
            }
        }
        return nearestPlanet;
    }
}
