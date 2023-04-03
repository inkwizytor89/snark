package org.enoch.snark.module.collector;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.model.ColonyPlaner;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;

import static org.enoch.snark.instance.config.Config.MAIN;

public class CollectorThread extends AbstractThread {

    public static final String threadName = "collector";
    public static final String COLLECTION_DESTINATION = "coll_dest";

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return 60;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStep() {
        if(noCollectingOngoing()) {
            ColonyEntity destination = getCollectionDestinationFromConfig();
            FleetEntity fleet = buildCollectingFleetEntity(destination);

            fleet.metal = fleet.source.metal;
            fleet.crystal = fleet.source.crystal;
            long deuterium = fleet.source.deuterium < 4000000L? 0: fleet.source.deuterium;
            fleet.deuterium = deuterium;

            SendFleetCommand collecting = new SendFleetCommand(fleet);
            collecting.addTag(threadName);
            commander.push(collecting);
        }
    }

    private FleetEntity buildCollectingFleetEntity(ColonyEntity destination) {
        FleetEntity fleet = new FleetEntity();
        fleet.mission = Mission.TRANSPORT;
        fleet.source = getColonyToCollect(destination);
        fleet.targetGalaxy = destination.galaxy;
        fleet.targetSystem = destination.system;
        fleet.targetPosition = destination.position;
        fleet.spaceTarget = destination.isPlanet ? ColonyType.PLANET: ColonyType.MOON;
        fleet.transporterLarge = fleet.source.calculateTransportByTransporterLarge();
        return fleet;
    }

    private ColonyEntity getColonyToCollect(ColonyEntity destination) {
        ColonyEntity source = null;
        for(ColonyEntity fp : instance.flyPoints) {
            ColonyEntity colony = ColonyDAO.getInstance().fetch(fp);
            if(colony.cp.equals(destination.cp)) continue;
            if(calculateResources(colony) > calculateResources(source) && canItTransport(colony)) {
                source = colony;
            }
        }
        return source;
    }

    private boolean noCollectingOngoing() {
        return noCollectingByNavigator() && noWaitingElementsByTag(threadName) && noActiveCollectingInDB();
    }

    private boolean noActiveCollectingInDB() {
        return fleetDAO.findLastSend(LocalDateTime.now().minusHours(8)).stream()
                .filter(fleet -> Mission.TRANSPORT.equals(fleet.mission))
                .noneMatch(fleetEntity -> fleetEntity.start == null ||
                        LocalDateTime.now().isBefore(fleetEntity.back));
    }

    private ColonyEntity getCollectionDestinationFromConfig() {
        String config = Instance.config.getConfig(threadName, COLLECTION_DESTINATION, StringUtils.EMPTY);
        if(config == null || config.isEmpty()) {
            long oneBeforeLast = Instance.config.getConfigLong(MAIN, Config.GALAXY_MAX, 6L)-1;
            return new ColonyPlaner(new Planet("["+oneBeforeLast+":325:8]")).doNotSkipPlanet().getNearestColony();
        } else {
            return new ColonyPlaner(new Planet(config)).doNotSkipPlanet().getNearestColony();
        }
    }

    private boolean canItTransport(ColonyEntity colony) {
        return colony.transporterLarge >= colony.calculateTransportByTransporterLarge();
    }

    private long calculateResources(ColonyEntity colony) {
        if(colony == null) return 0L;
        else return colony.metal + colony.crystal*2 + colony.deuterium*3;
    }

    private boolean noCollectingByNavigator() {
        return Navigator.getInstance().noneMission(Mission.TRANSPORT);
    }
}
