package org.enoch.snark.gi.command.request;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.impl.FleetDAOImpl;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.SpyObserver;
import org.enoch.snark.gi.command.impl.ReadSpyInfoCommand;
import org.enoch.snark.gi.command.impl.SpyCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SpyInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpyRequest {

    private final FleetDAO fleetDAO;
    private final Long code;

    public SpyRequest(Instance instance, List<TargetEntity> targets) {
        fleetDAO = new FleetDAOImpl(instance.universeEntity);
        code = fleetDAO.genereteNewCode();

        for(TargetEntity target : targets) {
            FleetEntity fleet = FleetEntity.createSpyFleet(instance, target);
            fleet.code = code;
            fleetDAO.saveOrUpdate(fleet);
        }
    }

    public boolean isFinished() {
        List<FleetEntity> requesedFleet = fleetDAO.findWithCode(code);
        LocalDateTime now = LocalDateTime.now();
        return requesedFleet.stream().allMatch(
                fleetEntity -> fleetEntity.visited != null && now.isAfter(fleetEntity.visited));
    }

    public Long getCode() {
        return code;
    }

    public List<SpyInfo> returnSpyReport() {
        return null;
    }
}
