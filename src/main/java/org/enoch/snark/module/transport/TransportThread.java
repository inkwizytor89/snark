package org.enoch.snark.module.transport;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.Resources;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.ConfigMap;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.enoch.snark.gi.macro.Mission.TRANSPORT;
import static org.enoch.snark.gi.macro.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.model.Resources.everything;

public class TransportThread extends AbstractThread {

    protected static final Logger LOG = Logger.getLogger(TransportThread.class.getName());
    public static final String threadName = "transport";

    public TransportThread(ConfigMap map) {
        super(map);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        pause = 60;
        List<ColonyEntity> sources = map.getFlyPoints();
        for(ColonyEntity colony : sources) {

            if(DateUtil.isExpired(colony.updated, 2L, ChronoUnit.HOURS)) {
                OpenPageCommand command = new OpenPageCommand(FLEETDISPATCH, colony);
                command.addTag(command.toString()).push(command.toString());
                System.out.println(threadName+" first check "+command.toString());
                continue;
            }

            Resources resources = colony.getResources();
            if(resources.isMoreThan("2m") && isNumberOfShipsReasonable(colony) && colony.cpm != null) {
                FleetEntity fleetEntity = creteFleetToTransport(colony);
                SendFleetCommand command = new SendFleetCommand(fleetEntity);
                command.addTag(threadName);
                command.setResources(everything);
                command.push();
            }
        }
    }

    private boolean isNumberOfShipsReasonable(ColonyEntity colony) {
        return colony.calculateTransportByTransporterSmall() < (colony.transporterSmall + 5 * colony.transporterLarge) *8;
    }

    private FleetEntity creteFleetToTransport(ColonyEntity colony) {
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        shipsMap.put(ShipEnum.transporterSmall, colony.transporterSmall);
        shipsMap.put(ShipEnum.transporterLarge, colony.transporterLarge);

        FleetEntity fleetEntity = new FleetEntity();
        fleetEntity.source = colony;
        fleetEntity.setTarget(ColonyDAO.getInstance().find(colony.cpm).toPlanet());
        fleetEntity.mission = TRANSPORT;
        fleetEntity.setShips(shipsMap);
        fleetEntity.metal = Long.MAX_VALUE;
        fleetEntity.crystal = Long.MAX_VALUE;
        fleetEntity.deuterium = Long.MAX_VALUE;
        return fleetEntity;
    }
}
