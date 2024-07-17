package org.enoch.snark.instance.model.action.filter;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;
import org.enoch.snark.gi.types.Mission;

import java.time.LocalDate;
import java.util.*;

public class UnusedExpeditionFilter extends AbstractFilter {

    @Override
    public List<SendFleetPromiseCommand> filter(List<SendFleetPromiseCommand> fleets) {
        if(fleets.isEmpty()) return fleets;
        List<ColonyEntity> sources = fleets.stream().map(f -> f.promise().getSource()).toList();
        Optional<Map.Entry<ColonyEntity, Integer>> min = createExpCountMap().entrySet().stream()
                .filter(entry -> sources.contains(entry.getKey()))
                .min(Comparator.comparingInt(Map.Entry::getValue));
        if(min.isEmpty()) return new ArrayList<>();
        ColonyEntity colonyToSend = min.get().getKey();

        return fleets.stream()
                .filter(f -> colonyToSend.equals(f.promise().getSource()))
                .toList();
    }

    private static Map<ColonyEntity, Integer> createExpCountMap() {
        Map<ColonyEntity, Integer> expCount = new HashMap<>();
        FleetDAO.getInstance().fetchAll().stream()
                .filter(fleetEntity -> Mission.EXPEDITION.equals(fleetEntity.mission))
                .filter(fleetEntity -> fleetEntity.visited != null)
                .filter(fleetEntity -> LocalDate.now().isEqual(fleetEntity.visited.toLocalDate()))
                .forEach(fleetEntity -> {
                    expCount.putIfAbsent(fleetEntity.source,0);
                    expCount.put(fleetEntity.source, expCount.get(fleetEntity.source)+1);
                });
        return expCount;
    }
}
