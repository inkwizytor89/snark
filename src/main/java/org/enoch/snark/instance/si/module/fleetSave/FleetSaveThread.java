package org.enoch.snark.instance.si.module.fleetSave;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.PlanetCache;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.db.entity.FleetEntity.FLEET_SAVE_CODE;
import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.si.module.ThreadMap.SOURCE;

public class FleetSaveThread extends AbstractThread {

    public static final String threadType = "fleetSave";
    public static final int SOURCE_INDEX = 0;
    public static final int SPEED_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final String FS_KEY = "fs";
    private ColonyDAO colonyDAO;

    public FleetSaveThread(ThreadMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return 120;
    }

    @Override
    public int getRequestedFleetCount() {
        return PlanetCache.get(SOURCE).size();
    }

    @Override
    protected void onStart() {
        super.onStart();
        colonyDAO = ColonyDAO.getInstance();
    }

    @Override
    protected void onStep() {
        System.err.println("\nFs lap:");
        for(FleetEntity fleet : loadFleetToSave()) {
            if(!isColonyShipOnColony(fleet)) {
                System.err.println(fleet.source + " missing colonyShip - fs waiting");
                continue;
            }
            if(isColonyStillBlocked(fleet.source)) {
                System.err.println(fleet.source + " waiting for fleet - fs waiting");
                continue;
            }

            String colonizationCode = getColonizationCode(fleet);
            if(consumer.noBlockingHashInQueue(colonizationCode)) {
                SendFleetCommand command = new SendFleetCommand(fleet);
                command.promise().setShipsMap(ShipsMap.ALL_SHIPS);
                command.promise().setResources(everything);
                command.hash(colonizationCode);
                command.push();
                System.err.println(fleet.source+" push to send with code "+colonizationCode);
            }
        }
    }

//    private void loadFlyPoints() {
//        instance.getFlyPoints().forEach(col -> new OpenPageCommand(FLEETDISPATCH, col).push());
//        System.err.println("reloading fleets points");
//        SleepUtil.secondsToSleep(instance.getFlyPoints().size() * 10L);
//    }

    private String getColonizationCode(FleetEntity fleet) {
        return threadType +fleet.getDestination();
    }

    private boolean isColonyStillBlocked(ColonyEntity source) {
        return Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet ->
                        Mission.TRANSPORT.equals(fleet.mission) ||
                        Mission.ATTACK.equals(fleet.mission) ||
//                        Mission.STATIONED.equals(fleet.mission) ||
                        Mission.EXPEDITION.equals(fleet.mission)
                )
                .anyMatch(fleet -> source.toPlanet().equals(fleet.getEndingPlanet()));
    }

    private boolean isColonyShipOnColony(FleetEntity fleet) {
        ColonyEntity source = ColonyDAO.getInstance().fetch(fleet.source);
        return source.colonyShip > 0;
    }

    /**
     * fs=m[1:1:8]-30-p[1:1:1];m[2:2:8]-30-p[2:2:1]
     */
    private List<FleetEntity> loadFleetToSave() {
        List<String> allFleetToSaveConfig = map.getConfigArray(FS_KEY);
        List<FleetEntity> fleetToSave = new ArrayList<>();
        for(String fleetToSaveConfig : allFleetToSaveConfig){
            String[] configValues = fleetToSaveConfig.split("-");
            ColonyEntity source = colonyDAO.find(configValues[SOURCE_INDEX]);
            Planet target = new Planet(configValues[DESTINATION_INDEX]);

            FleetEntity fleetEntity = FleetEntity.createQuickFleetSave(source, target);
            fleetEntity.speed = Long.parseLong(configValues[SPEED_INDEX]);
            fleetEntity.code = FLEET_SAVE_CODE;
            fleetToSave.add(fleetEntity);
        }
        return fleetToSave;
    }
}
