package org.enoch.snark.instance.si.module.fleet;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.util.Map;

import static org.enoch.snark.db.entity.FleetEntity.FLEET_THREAD;
import static org.enoch.snark.gi.types.Mission.STATIONED;
import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.types.QueueRunType.FLEET_ACTION_WITH_PRIORITY;
import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class FleetThread extends AbstractThread {

    public static final String threadName = "fleet";

    public FleetThread(ConfigMap map) {
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStep() {
        int index = 0;
        for(Map<ShipEnum, Long> shipWave : map.getShipsWaves()) {
            index++;
            for (ColonyEntity colony : map.getColonies(SOURCE, "planet")) {
                FleetEntity fleetEntity = new FleetEntity();
                fleetEntity.setShips(shipWave);
                fleetEntity.source = colony;
                generateTarget(fleetEntity);
                generateMission(fleetEntity);
                generateSpeed(fleetEntity);
                fleetEntity.code = FLEET_THREAD;

                // expresion target
                // Expedition is probably back 2024-01-06T15:12:27.609 back is 2024-01-06T15:12:17
                // ustawić co ile ma to zresetować sprawdzanie
                // ustawić tag na warunek aktywacji

                // co z finally na zamkniecie tranzakicji w db
                // obiekt na zarzadzanie fala floty
                String tagKey = generateTagKey(fleetEntity, index);
                if(shouldFleetBeSend(tagKey)) {
                    SendFleetCommand command = new SendFleetCommand(fleetEntity);
                    command.addTag(threadName);
                    command.addTag(tagKey);
                    command.setResources(map.getConfigResource(RESOURCES, nothing));
                    command.setAllShips(shipWave.isEmpty());
                    command.setRunType(FLEET_ACTION_WITH_PRIORITY);
                    command.push();
                }
            }
        }
    }

    private boolean shouldFleetBeSend(String tagKey) {
        boolean noWaitingElement = Commander.getInstance().noWaitingElementsByTag(tagKey);
        if(!noWaitingElement) return false;
//        FleetDAO.getInstance().findLastSend()
        return false;
    }

    private void generateTarget(FleetEntity fleetEntity) {
        fleetEntity.setTarget(map.getConfigPlanet(null));
    }

    private void generateMission(FleetEntity fleetEntity) {
        fleetEntity.mission = Mission.valueOf(map.getConfig(MISSION, STATIONED.name()));
    }

    private void generateSpeed(FleetEntity fleetEntity) {
        fleetEntity.speed = map.getConfigLong(SPEED, null);
    }

    private String generateTagKey(FleetEntity fleetEntity, int index) {
        return map.getConfig(NAME+"_"+fleetEntity.source+"_"+fleetEntity.mission+"_"+
                fleetEntity.getTarget()+"_"+index);
    }

}
