package org.enoch.snark.instance.si.module.expedition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.FleetSendCommand;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.model.technology.Ship;
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
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
// na start mogl by przeleceic swoje flypointy
// jak sa starsze niz 4h to powinien go sobie zaktualizowac flypoint
// z tych co zostały znajdz najlepszego ?

@Slf4j
@RequiredArgsConstructor
public class ExpeditionThread extends AbstractThread {

    public static final String threadType = "expedition";
    public static final String BATTLE_EXTENSION = "battle_extension";
    public static final String MAX_DT = "max_dt";
    public static final ShipsMap DEFAULT_SHIPS = ShipsMap.parse("explorer:1,transporterLarge:2500,battleship:1");

    private final FleetRepository fleetRepository;
    private final FleetDispatcher fleetDispatcher;

    private Queue<ColonyEntity> expeditionSource = new LinkedList<>();
    private Long maxTL;

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
    protected void onStart() {
        super.onStart();
    }

    private void chooseColoniesForExpeditionsStart() {
        expeditionSource = new LinkedList<>();
        expeditionSource.addAll(map.getSources());
        loadExpeditionPoints();
    }

    @Override
    protected void onStep() {

        if (noFreeSlotsForExpedition()) return;
        if (waitingForExecution()) return;

        ColonyEntity bestColony;
        List<ColonyEntity> source = getSources(PlanetService.ALL);
        ShipsMap requiredShipMap = map.getShips(DEFAULT_SHIPS);
        if (enoughShipsForAllRequiredExpeditions(source, requiredShipMap)) {
            List<ColonyEntity> possibleColonies = canSendExpeditons(source, requiredShipMap);
            bestColony = findBest(possibleColonies);
        } else {
            // magic
            List<ColonyEntity> possibleColonies = possibleColonies(source);
            bestColony = specifyBestColony(possibleColonies);
            requiredShipMap = specifyShips(bestColony, possibleColonies.size());
            if(NO_SHIPS.equals(requiredShipMap)) {
                throw new RuntimeException("Can not specify any expedition from "+source);
            }
        }
        pushCommand(createFleetSendCommand(bestColony, requiredShipMap));
        pause.update("1S");
        //-------------------


////        Boolean waiting = map.getConfigBoolean("waiting", true);
////        if(waiting && stillWaitingForFleet()) return;
////        if(stillWaitingForFleet()) return;
//        if (noFreeSlotsForExpedition() && noWaitingExpedition()) {
//            ColonyEntity colony = getSource();
////            String collect = expeditionSource.stream().map(PlanetEntity::toString).collect(Collectors.joining(", "));
////            System.err.println("lista expów: "+collect);
//            if (colony == null) return;
//            colony = ColonyDAO.getInstance().fetch(colony);
//            FleetEntity expedition = buildExpeditionFleet(colony);
//            if(expedition != null) {
//                setExpeditionReadyToStart(expedition);
//            }
//        }
    }

    private ShipsMap specifyShips(ColonyEntity colony, int size) {
        int expeditionFreeSlots = Navigator.getExpeditionFreeSlots();
        int divisor = Math.floorDiv(size + expeditionFreeSlots - 1, expeditionFreeSlots);
        Long transportUnitCount = colony.getShipsMap().getTransportUnitCount() / divisor;

        ShipsMap shipsMap = new ShipsMap();
        if (colony.explorer > 0) shipsMap.put(explorer, 1L);
        if(divisor == 1) {
            shipsMap.put(transporterSmall, colony.transporterSmall);
            shipsMap.put(transporterLarge, colony.transporterLarge);
        } else {
            long neededLargeTransporter = transportUnitCount / 5;
            if(colony.transporterLarge >= neededLargeTransporter) {
                shipsMap.put(transporterLarge, neededLargeTransporter);
            } else {
                shipsMap.put(transporterLarge, colony.transporterLarge);
                transportUnitCount -= 5 * colony.transporterLarge;
                shipsMap.put(transporterSmall, transportUnitCount);
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
                .shipsWaves(singletonList(ships))
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
        List<ColonyEntity> moons = minColonies.stream().filter(colonyEntity -> ColonyType.MOON.equals(colonyEntity.type)).collect(Collectors.toList());

        if(!moons.isEmpty()) return moons.getFirst();
        return minColonies.getFirst();
    }

    private List<ColonyEntity> canSendExpeditons(List<ColonyEntity> source, ShipsMap ships) {
        // hasEnoughShips
        // hasEnoughtDeuter
        // without expedition if can excape from attack
        ShipsMap requiredShips = new ShipsMap();
        if(ships.containsKey(explorer)) requiredShips.put(explorer, ships.shipCount(explorer));
        if(ships.containsKey(transporterLarge))  requiredShips.put(transporterLarge, ships.shipCount(transporterLarge));
        if(ships.containsKey(transporterSmall))  requiredShips.put(transporterSmall, ships.shipCount(transporterSmall));

        return source.stream().filter(colony -> colony.hasEnoughShips(requiredShips)).collect(Collectors.toList());
    }

    private List<ColonyEntity> possibleColonies(List<ColonyEntity> source) {
        List<ColonyEntity> bestColonies = source.stream().filter(colony -> colony.explorer > 0).collect(Collectors.toList());
        return bestColonies.isEmpty() ? source : bestColonies;
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
        int potentialExpedition = 0;

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

    private ColonyEntity getSource() {
        if(expeditionSource.isEmpty()) chooseColoniesForExpeditionsStart();
        ColonyEntity poll = expeditionSource.poll();
        if(poll == null) {
            System.err.println("expeditionQueue in ExpeditionThread is empty");
            chooseColoniesForExpeditionsStart();
            return null;
        } else {
            expeditionSource.add(poll);
            return ColonyDAO.getInstance().fetch(poll);
        }
    }

    private boolean noWaitingExpedition() {
        throw new NotImplementedException("To fix in spring version");
//        return consumer.noBlockingHashInQueue(threadType);
    }

    private boolean noFreeSlotsForExpedition() {
        return Navigator.getExpeditionFreeSlots() <= 0;
    }

    private FleetEntity buildExpeditionFleet(ColonyEntity colony) {
        maxTL = calculateMaxExpeditionSize();
        FleetEntity expeditionToSend = FleetEntity.createExpeditionDirection(colony);

        ShipsMap requestedExpeditionShipMap = Ship.createExpeditionShipMap(maxTL, 0L, 1L);
        if(colony.hasEnoughShips(requestedExpeditionShipMap)) {

            expeditionToSend.setShips(requestedExpeditionShipMap);
            return expeditionToSend;
        }

        if(anyExpeditionStartPointHasEnoughShips(requestedExpeditionShipMap)) {
            return null;
        } else if(allExpeditionStartPointHasNoneTransporters()) {
            return null;
        }
        return sendWhatYouCan();
    }

    private boolean allExpeditionStartPointHasNoneTransporters() {
        refreshColoniesFromDb();
        return expeditionSource.stream().allMatch(colony -> colony.transporterLarge<1 && colony.transporterSmall<1);
    }

    private void refreshColoniesFromDb() {
        expeditionSource = expeditionSource.stream()
        .map(colonyEntity -> ColonyDAO.getInstance().fetch(colonyEntity))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean anyExpeditionStartPointHasEnoughShips(ShipsMap expeditionMap) {
        refreshColoniesFromDb();
        return expeditionSource.stream().anyMatch(colony -> colony.hasEnoughShips(expeditionMap));
    }

    private void loadExpeditionPoints() {
        refreshColoniesFromDb();
        expeditionSource.stream()
                .filter(colonyEntity -> DateUtil.isExpired2H(colonyEntity.updated))
                .forEach(col -> {
                    new OpenPageCommand(FLEETDISPATCH, col).hash(threadType +"_"+col).push();
                });
        System.err.println("reloading expedition points");
        SleepUtil.secondsToSleep(expeditionSource.size() * 5L);
    }

    private FleetEntity sendWhatYouCan() {
        System.err.println("\nsendWhatYouCan expedition - why?!\n");
        ColonyEntity anotherExpeditionStartPoint = findBestExpeditionStartPoint();
        FleetEntity expedition = FleetEntity.createExpeditionDirection(anotherExpeditionStartPoint);
        if(anotherExpeditionStartPoint.explorer > 0) {
            expedition.explorer = 1L;
        }
        if(anotherExpeditionStartPoint.transporterLarge > maxTL) {
            expedition.transporterLarge = maxTL;
        } else {// send what you have
            expedition.transporterLarge = anotherExpeditionStartPoint.transporterLarge;
            long missingLT = (maxTL - expedition.transporterLarge) * 5;
            Long actualTransporterSmall = anotherExpeditionStartPoint.transporterSmall;
            expedition.transporterSmall = missingLT>actualTransporterSmall?actualTransporterSmall:missingLT;
        }
        return expedition;
    }

    private ColonyEntity findBestExpeditionStartPoint() {
        ColonyEntity bestColony = null;
        long bestAmount = 0L;
        for (ColonyEntity expPoint : expeditionSource) {
            long amount = 5*expPoint.transporterLarge + expPoint.transporterSmall;
            if(amount > bestAmount) {
                bestAmount = amount;
                bestColony = expPoint;
            }
        }
        return bestColony;
    }

    public Long calculateMaxExpeditionSize() {
        return map.getConfigLong(MAX_DT, 2500L);
    }

    private void setExpeditionReadyToStart(FleetEntity expedition) {
        boolean battleExtension = map.getConfigBoolean(BATTLE_EXTENSION, true);
        throw new NotImplementedException("Missing logic for ExpeditionFleetCommand");
//        ExpeditionFleetCommand expeditionFleetCommand = new ExpeditionFleetCommand(expedition, battleExtension);
//        expeditionFleetCommand.hash(threadType);
//        expeditionFleetCommand.push();
    }
}
