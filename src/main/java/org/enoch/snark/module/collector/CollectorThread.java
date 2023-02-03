package org.enoch.snark.module.collector;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.model.types.MissionType;
import org.enoch.snark.module.AbstractThread;

public class CollectorThread extends AbstractThread {

    public static final String threadName = "Collector";

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
        if(false && noCollecting() && noWaitingElementsByTag(threadName)) {

            ColonyEntity destination = null; //from config
            ColonyEntity source = null;

            for(ColonyEntity fp : instance.flyPoints) {
                ColonyEntity colony = ColonyDAO.getInstance().fetch(fp);
                if(colony.cp == destination.cp) continue;
                if(calculateResources(colony) > calculateResources(source) && canItTransport(colony)) {
                    source = colony;
                }
            }

            FleetEntity fleet = new FleetEntity();
            fleet.type = MissionType.TRANSPORT.getName();
            fleet.source = source;
            fleet.targetGalaxy = destination.galaxy;
            fleet.targetSystem = destination.system;
            fleet.targetPosition = destination.position;
            fleet.spaceTarget = destination.isPlanet ? ColonyType.PLANET.getName(): ColonyType.MOON.getName();
            fleet.transporterLarge = source.calculateTransportByTransporterLarge();

            Long deuterium = source.deuterium < 1000000L? 0: source.deuterium-1000000L ;
            new Resources(source.metal, source.crystal, deuterium); // dodac jako element do przewiezienia

            SendFleetCommand collecting = new SendFleetCommand(fleet);
            collecting.addTag(threadName);
        }
    }

    private boolean canItTransport(ColonyEntity colony) {
        return colony.transporterLarge >= colony.calculateTransportByTransporterLarge();
    }

    private long calculateResources(ColonyEntity colony) {
        if(colony == null) return 0L;
        else return colony.metal + colony.crystal*2 + colony.deuterium*3;
    }

    private boolean noCollecting() {
        return Navigator.getInstance().getEventFleetList().stream()
                .noneMatch(fleet -> MissionType.TRANSPORT.equals(fleet.missionType));
    }
}
