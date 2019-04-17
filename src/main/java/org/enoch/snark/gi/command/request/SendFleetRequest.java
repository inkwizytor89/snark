package org.enoch.snark.gi.command.request;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.impl.FleetDAOImpl;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.exception.TargetMissingResourceInfoException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendFleetRequest {

    private Instance instance;
    private final FleetDAO fleetDAO;
    private List<TargetEntity> targets;
    private String mission;
    private Long code;
    private Integer limit;
    private int sendetFleet = 0;

    public SendFleetRequest(Instance instance, String mission, List<TargetEntity> targets) {
        fleetDAO = new FleetDAOImpl(instance.universeEntity);
        this.instance = instance;
        this.targets = new ArrayList<>(targets);
        this.mission = mission;
        this.limit = targets.size();
    }

    private boolean isFinished() {
        List<FleetEntity> requesedFleet = fleetDAO.findWithCode(code);
        return requesedFleet.stream().allMatch(FleetEntity::isDone);
    }

    public Long sendAndWait() {
        code = fleetDAO.genereteNewCode();
        // send x fleet

        do{
            sendLimitFleet(limit - sendetFleet);
            while(!isFinished()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        } while (canSendMore());
        return code;
    }

    private void sendLimitFleet(int limit) {
        ArrayList<TargetEntity> toSend = new ArrayList<>();

        for(TargetEntity target : targets) {
            try {
                if (limit <= 0) break;
                toSend.add(target);
                limit--;
            } catch (TargetMissingResourceInfoException e) {
                e.printStackTrace();
            }
        }

        for (TargetEntity target : toSend) {
            FleetEntity fleet = generateFleet(target);
            fleet.code = code;
            fleetDAO.saveOrUpdate(fleet);
            targets.remove(target);
        }
    }

    private boolean canSendMore() {
        List<FleetEntity> requesedFleet = fleetDAO.findWithCode(code);
        sendetFleet = requesedFleet.size();
        return !targets.isEmpty() && sendetFleet < limit;
    }

    private FleetEntity generateFleet(TargetEntity target) {
        if(FleetEntity.SPY.equals(mission)) {
            return FleetEntity.createSpyFleet(instance, target);
        }
        if(FleetEntity.ATTACK.equals(mission)) {
            return FleetEntity.createFarmFleet(instance, target);
        }
        throw new RuntimeException("Unknown mission "+ mission);
    }

    public SendFleetRequest setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }
}
