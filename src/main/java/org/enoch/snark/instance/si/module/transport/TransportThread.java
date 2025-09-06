package org.enoch.snark.instance.si.module.transport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.TRANSPORT;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.to.Resources.everything;

@RequiredArgsConstructor
public class TransportThread extends AbstractThread {

    public static final String threadType = "transport";
    private int threadPause = 60;

//    public TransportThread(ThreadMap map) {
//        super(map);
//    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return threadPause+"S";
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
        throw new NotImplementedException("To remove in spring version");
//        threadPause = 60;
//        List<ColonyEntity> sources = map.getSources();
//        for(ColonyEntity colony : sources) {
//
//            if(DateUtil.isExpired(colony.updated, 2L, ChronoUnit.HOURS)) {
//                OpenPageCommand command = new OpenPageCommand(FLEETDISPATCH, colony);
//                command.hash(command.toString()).push();
//                System.out.println(threadType +" first check "+command.toString());
//                continue;
//            }
//
//            Resources resources = colony.getResources();
//            if(resources.isCountMoreThan("2m") && isNumberOfShipsReasonable(colony) && colony.cpm != null) {
//                FleetPromise fleetEntity = creteFleetToTransport(colony);
//                SendFleetPromiseCommand command = new SendFleetPromiseCommand(fleetEntity);
//                command.hash(threadType);
//                command.promise().setResources(everything);
//                command.push();
//            }
//        }
    }

    private boolean isNumberOfShipsReasonable(ColonyEntity colony) {
        return colony.calculateTransportByTransporterSmall() < (colony.transporterSmall + 5 * colony.transporterLarge) *8;
    }

    private FleetPromise creteFleetToTransport(ColonyEntity colony) {
        ShipsMap shipsMap = new ShipsMap();
        shipsMap.put(Ship.transporterSmall, colony.transporterSmall);
        shipsMap.put(Ship.transporterLarge, colony.transporterLarge);

        FleetPromise fleetPromise = new FleetPromise();
        fleetPromise.setSource(colony);
        fleetPromise.setTarget(ColonyDAO.getInstance().find(colony.cpm).toPlanet());
        fleetPromise.setMission(TRANSPORT);
        fleetPromise.setShipsMap(shipsMap);
        fleetPromise.setResources(everything);
        return fleetPromise;
    }
}
