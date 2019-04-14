package org.enoch.snark.gi.command.request;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.impl.FleetDAOImpl;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.exception.TargetMissingResourceInfoException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendFleetRequest {

    private Instance instance;
    private final FleetDAO fleetDAO;
    private List<TargetEntity> targets;
    private String mission;
    private Long code;
    private Integer limit;

    public SendFleetRequest(Instance instance, String mission, List<TargetEntity> targets) {
        fleetDAO = new FleetDAOImpl(instance.universeEntity);
        this.instance = instance;
        this.targets = targets;
        this.mission = mission;
        this.limit = targets.size();
    }

    private boolean isFinished() {
        List<FleetEntity> requesedFleet = fleetDAO.findWithCode(code);
        return requesedFleet.stream().allMatch(FleetEntity::isDone);
    }

    public Long sendAndWait() {
        code = fleetDAO.genereteNewCode();
        for(TargetEntity target : targets) {
            try {
                if (limit <= 0) break;
                FleetEntity fleet = generateFleet(target);
                fleet.code = code;
                fleetDAO.saveOrUpdate(fleet);
                limit--;
            } catch (TargetMissingResourceInfoException e) {
                e.printStackTrace();
            }
        }
        while(!isFinished()) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return code;
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
