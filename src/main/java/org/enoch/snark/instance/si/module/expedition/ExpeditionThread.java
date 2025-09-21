package org.enoch.snark.instance.si.module.expedition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.model.to.FleetPlan;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.enoch.snark.instance.model.technology.Ship.*;
import static org.enoch.snark.instance.model.to.ShipsMap.NO_SHIPS;

@Slf4j
@RequiredArgsConstructor
public class ExpeditionThread extends AbstractThread {

    public static final String threadType = "expedition";
    public static final ShipsMap DEFAULT_SHIPS = ShipsMap.parse("explorer:1,transporterLarge:2500,battleship:1");

    private final FleetRepository fleetRepository;
    private final FleetDispatcher fleetDispatcher;

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return "8S";
    }

    @Override
    public int getRequestedFleetCount() {
        return Navigator.getExpeditionMax();
    }

    @Override
    protected void onStep() {
        if (noFreeSlotsForExpedition()) return;
        else  pause.update("1S");
        if (waitingForExecution()) return;

        ColonyEntity bestColony;
        List<ColonyEntity> source = getSources(PlanetService.ALL);
        ShipsMap requiredShipMap = map.getShips(DEFAULT_SHIPS);
        if (enoughShipsForAllRequiredExpeditions(source, requiredShipMap)) {
            List<ColonyEntity> possibleColonies = canSendExpeditons(source, requiredShipMap);
            bestColony = findBest(possibleColonies);
        } else {
            List<ColonyEntity> possibleColonies = possibleColonies(source);
            bestColony = specifyBestColony(possibleColonies);
            requiredShipMap = specifyShips(bestColony, possibleColonies);
            if(NO_SHIPS.equals(requiredShipMap)) {
                throw new RuntimeException("Can not specify any expedition from "+source);
            }
        }
        pushCommand(createFleetSendCommand(bestColony, requiredShipMap));
    }

    private ShipsMap specifyShips(ColonyEntity colony, List<ColonyEntity> colonies) {
        int expeditionFreeSlots = Navigator.getExpeditionFreeSlots();

        double sum = colonies.stream()
                .mapToLong(colonyEntity -> colonyEntity.getShipsMap().getTransportUnitCount())
                .sum();
        long avgExpeditionTransportUnit = Math.round(sum / expeditionFreeSlots);

        Long colonyTransportUnitCount = colony.getShipsMap().getTransportUnitCount();
        long expeditionCount = Math.round((double) colonyTransportUnitCount / avgExpeditionTransportUnit);

        ShipsMap shipsMap = new ShipsMap();
        if (colony.explorer > 0) shipsMap.put(explorer, 1L);
        if(expeditionCount == 1) {
            shipsMap.put(transporterSmall, colony.transporterSmall);
            shipsMap.put(transporterLarge, colony.transporterLarge);
        } else {
            long singleExpeditionTransportUnit = colonyTransportUnitCount / expeditionCount;
            long neededLargeTransporter = (long) Math.ceil((double) singleExpeditionTransportUnit / 5);
            if(colony.transporterLarge >= neededLargeTransporter) {
                shipsMap.put(transporterLarge, neededLargeTransporter);
            } else {
                shipsMap.put(transporterLarge, colony.transporterLarge);
                singleExpeditionTransportUnit -= 5 * colony.transporterLarge;
                shipsMap.put(transporterSmall, singleExpeditionTransportUnit);
            }
        }
        return shipsMap;
    }

    private FleetSendCommand createFleetSendCommand(ColonyEntity bestColony, ShipsMap ships) {
        Planet expeditionTarget = bestColony.toPlanet();
        expeditionTarget.position = 16;

        FleetPlan fleetPlan = FleetPlan.builder()
                .source(bestColony.toString())
                .target(expeditionTarget.toString())
                .mission(Mission.EXPEDITION)
                .ships(ships)
                .build();
        return fleetDispatcher.from(fleetPlan).getFirst();
    }

    private ColonyEntity findBest(List<ColonyEntity> colonies) {
        List<FleetEntity> historicExpedition = fleetRepository.findAll().stream()
                .filter(fleet -> Mission.EXPEDITION.equals(fleet.mission))
                .filter(fleet -> fleet.visited.getDayOfYear() == LocalDateTime.now().getDayOfYear())
                .filter(fleet -> colonies.stream().map(colony -> new Planet(colony.galaxy, colony.system, 16, ColonyType.PLANET)).toList()
                        .contains(fleet.getTarget()))
                .toList();

        Map<ColonyEntity, Long> countMap = new HashMap<>();
        for (ColonyEntity colony : colonies) {
            long count = historicExpedition.stream()
                    .filter(fleetEntity -> fleetEntity.targetGalaxy.equals(colony.galaxy))
                    .filter(fleetEntity -> fleetEntity.targetSystem.equals(colony.system))
                    .count();
            countMap.put(colony, count);
        }

        long min = countMap.values().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElseThrow();
        List<ColonyEntity> minColonies = countMap.entrySet().stream()
                .filter(e -> e.getValue().equals(min))
                .map(Map.Entry::getKey)
                .toList();
        List<ColonyEntity> moons = minColonies.stream()
                .filter(colonyEntity -> ColonyType.MOON.equals(colonyEntity.type)).toList();

        if(!moons.isEmpty()) return moons.getFirst();
        return minColonies.getFirst();
    }

    private List<ColonyEntity> canSendExpeditons(List<ColonyEntity> source, ShipsMap ships) {
        // todo hasEnoughShips
        // todo hasEnoughtDeuter
        // todo without expedition if can excape from attack
        ShipsMap requiredShips = new ShipsMap();
        if(ships.containsKey(explorer)) requiredShips.put(explorer, ships.shipCount(explorer));
        if(ships.containsKey(transporterLarge))  requiredShips.put(transporterLarge, ships.shipCount(transporterLarge));
        if(ships.containsKey(transporterSmall))  requiredShips.put(transporterSmall, ships.shipCount(transporterSmall));

        return source.stream().filter(colony -> colony.hasEnoughShips(requiredShips)).collect(Collectors.toList());
    }

    private List<ColonyEntity> possibleColonies(List<ColonyEntity> source) {
        List<ColonyEntity> withExplorer = source.stream().filter(colony -> colony.explorer > 0).collect(Collectors.toList());
        List<ColonyEntity> goodColonies = withExplorer.isEmpty() ? source : withExplorer;
        double sum = source.stream()
                .mapToLong(colonyEntity -> colonyEntity.getShipsMap().getTransportUnitCount())
                .sum();
        double limit = sum * 30 / 100 / source.size();
        List<ColonyEntity> possibleColonies = goodColonies.stream().filter(colony -> colony.getShipsMap().getTransportUnitCount() > limit).toList();
        if(possibleColonies.isEmpty()) {
            throw new IllegalStateException("No colonies with required transport limit "+limit+" "+ transporterSmall.name());
        }
        return possibleColonies;
    }

    private ColonyEntity specifyBestColony(List<ColonyEntity> source) {
        ColonyEntity best = source.getFirst();
        Long maxTransportUnitCount = best.getShipsMap().getTransportUnitCount();
        for(ColonyEntity colony : source) {
            Long transportUnitCount = colony.getShipsMap().getTransportUnitCount();
            if(maxTransportUnitCount < transportUnitCount) {
                maxTransportUnitCount = transportUnitCount;
                best = colony;
            }
        }
        return best;
    }

    private boolean enoughShipsForAllRequiredExpeditions(List<ColonyEntity> source, ShipsMap requiredShipMap) {
        int expeditionFreeSlots = Navigator.getExpeditionFreeSlots();
        long potentialExpedition = 0;

        Long requiredTransportUnitCount = requiredShipMap.getTransportUnitCount();

        for(ColonyEntity colony : source) {
            ShipsMap colonyShipsMap = colony.getShipsMap();
            Long colonyTransportUnitCount = colonyShipsMap.getTransportUnitCount();

            long count = colonyTransportUnitCount / requiredTransportUnitCount;
            Long explorerCount = colonyShipsMap.shipCount(explorer);

            if(explorerCount >= count) potentialExpedition += count;
            else potentialExpedition += explorerCount;

            if (potentialExpedition >= expeditionFreeSlots) return true;
        }
        return false;
    }

    private boolean noFreeSlotsForExpedition() {
        return Navigator.getExpeditionFreeSlots() <= 0;
    }
}
