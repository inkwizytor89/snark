package org.enoch.snark.module.fleetSave;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.Planet;
import org.enoch.snark.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.db.entity.FleetEntity.FLEET_SAVE_CODE;

public class FleetSaveThread extends AbstractThread {

    public static final String threadName = "fleetSave";
    public static final int SOURCE_INDEX = 0;
    public static final int SPEED_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final String FS_KEY = "fs";
    private ColonyDAO colonyDAO;

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return 6;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        colonyDAO = ColonyDAO.getInstance();
    }

    @Override
    protected void onStep() {
        for(FleetEntity fleet : loadFleetToSave()) {
            if(!isColonyShipOnColony(fleet)) continue;
            if(isColonyStillBlocked(fleet.source)) continue;

            String colonizationCode = getColonizationCode(fleet);
            if(noWaitingElementsByTag(colonizationCode)) {
                SendFleetCommand command = new SendFleetCommand(fleet);
                command.addTag(colonizationCode);
                commander.push(command);
            }
        }
    }

    private String getColonizationCode(FleetEntity fleet) {
        return threadName+fleet.getDestination();
    }

    private boolean isColonyStillBlocked(ColonyEntity source) {
        return Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet -> Mission.TRANSPORT.equals(fleet.mission) ||
                         Mission.ATTACK.equals(fleet.mission) ||
                         Mission.STATIONED.equals(fleet.mission) ||
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
        String[] allFleetToSaveConfig = Instance.config.getConfigArray(threadName, FS_KEY);
        List<FleetEntity> fleetToSave = new ArrayList<>();
        for(String fleetToSaveConfig : allFleetToSaveConfig){
            String[] configValues = fleetToSaveConfig.split("-");
            ColonyEntity source = colonyDAO.get(configValues[SOURCE_INDEX]);
            Planet target = new Planet(configValues[DESTINATION_INDEX]);

            FleetEntity fleetEntity = FleetEntity.createQuickColonization(source, target);
            fleetEntity.speed = Long.parseLong(configValues[SPEED_INDEX]);
            fleetEntity.code = FLEET_SAVE_CODE;
            fleetToSave.add(fleetEntity);
        }
        return fleetToSave;
    }
}
