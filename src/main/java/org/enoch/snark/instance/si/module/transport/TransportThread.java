package org.enoch.snark.instance.si.module.transport;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.enoch.snark.gi.types.Mission.TRANSPORT;
import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.to.Resources.everything;

public class TransportThread extends AbstractThread {

    public static final String threadType = "transport";

    public TransportThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadType;
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
        List<ColonyEntity> sources = map.getSources();
        for(ColonyEntity colony : sources) {

            if(DateUtil.isExpired(colony.updated, 2L, ChronoUnit.HOURS)) {
                OpenPageCommand command = new OpenPageCommand(FLEETDISPATCH, colony);
                command.hash(command.toString()).push();
                System.out.println(threadType +" first check "+command.toString());
                continue;
            }

            Resources resources = colony.getResources();
            if(resources.isCountMoreThan("2m") && isNumberOfShipsReasonable(colony) && colony.cpm != null) {
                FleetEntity fleetEntity = creteFleetToTransport(colony);
                SendFleetCommand command = new SendFleetCommand(fleetEntity);
                command.hash(threadType);
                command.promise().setResources(everything);
                command.push();
            }
        }
    }

    private boolean isNumberOfShipsReasonable(ColonyEntity colony) {
        return colony.calculateTransportByTransporterSmall() < (colony.transporterSmall + 5 * colony.transporterLarge) *8;
    }

    private FleetEntity creteFleetToTransport(ColonyEntity colony) {
        ShipsMap shipsMap = new ShipsMap();
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
