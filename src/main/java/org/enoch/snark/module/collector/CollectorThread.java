package org.enoch.snark.module.collector;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CollectorThread extends AbstractThread {

    public static final String threadName = "collector";
    public static final String COLLECTION_DESTINATION = "coll_dest";
    public static final String FLEET_SIZE = "fleet_size";

    public CollectorThread(ConfigMap map) {
        super(map);
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return 60;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        SleepUtil.sleep(TimeUnit.MINUTES, 10);
    }

    @Override
    protected void onStep() {
        if(notEnoughReadyPlanets()) { System.out.println(threadName+": not enough ready planets - sleep");SleepUtil.secondsToSleep(600); return;}
        if(isCollectingOngoing()) return;

        ColonyEntity destination = getCollectionDestinationFromConfig();
        if(destination == null) {
            System.err.println("Error: CollectorThread can not find destination colony");
            return;
        }
        FleetEntity fleet = buildCollectingFleetEntity(destination);

        Resources resources = new Resources();
        resources.metal = fleet.source.metal;
        resources.crystal = fleet.source.crystal;
        resources.deuterium = fleet.source.deuterium < 10000000L? 0: fleet.source.deuterium;

        SendFleetCommand collecting = new SendFleetCommand(fleet);
        collecting.setResources(resources);
        collecting.addTag(threadName);
        collecting.push();
    }

    private boolean notEnoughReadyPlanets() {
        List<ColonyEntity> flyPoints = instance.getFlyPoints();
        int minFlyPoints = flyPoints.size()/2;
        long readyPlanetCount = flyPoints.stream()
                .map(colony -> ColonyDAO.getInstance().fetch(colony))
                .filter(colony -> colony.transporterLarge > 0)
                .count();
        return readyPlanetCount < minFlyPoints;
    }

    private FleetEntity buildCollectingFleetEntity(ColonyEntity destination) {
        FleetEntity fleet = new FleetEntity();
        fleet.mission = Mission.TRANSPORT;
        fleet.source = getColonyToCollect(destination);
        fleet.targetGalaxy = destination.galaxy;
        fleet.targetSystem = destination.system;
        fleet.targetPosition = destination.position;
        fleet.spaceTarget = destination.isPlanet ? ColonyType.PLANET: ColonyType.MOON;
        fleet.transporterLarge = fleet.source.calculateTransportByTransporterLarge();
        return fleet;
    }

    private ColonyEntity getColonyToCollect(ColonyEntity destination) {
        ColonyEntity source = null;
        for(ColonyEntity fp : instance.flyPoints) {
            ColonyEntity colony = ColonyDAO.getInstance().fetch(fp);
            if(colony.cp.equals(destination.cp)) continue;
            if(calculateResources(colony) > calculateResources(source) && canItTransport(colony)) {
                source = colony;
            }
        }
        return source;
    }

    private boolean isCollectingOngoing() {
        return !(noCollectingByNavigator() && noWaitingElementsByTag(threadName) && noActiveCollectingInDB());
    }

    private boolean noActiveCollectingInDB() {
        return fleetDAO.findLastSend(LocalDateTime.now().minusHours(8)).stream()
                .filter(fleet -> Mission.TRANSPORT.equals(fleet.mission))
                .noneMatch(fleetEntity -> fleetEntity.start == null ||
                        LocalDateTime.now().isBefore(fleetEntity.back));
    }

    private ColonyEntity getCollectionDestinationFromConfig() {
        Planet configPlanet = map.getConfigPlanet(COLLECTION_DESTINATION);
        if(configPlanet == null) {
            Long fleetSize = map.getConfigLong(FLEET_SIZE, -1L);
            if(fleetSize == -1) return anyColony();
            Optional<Planet> planetOptional = Navigator.getInstance().getEventFleetList().stream()
                    .filter(eventFleet -> Long.parseLong(eventFleet.detailsFleet) > fleetSize)
                    .map(eventFleet -> eventFleet.getTo())
                    .findAny();
            if(!planetOptional.isPresent()) {
                String value = CacheEntryDAO.getInstance().getValue(COLLECTION_DESTINATION);
                if(value == null) return anyColony();
                ColonyEntity colonyEntity = new Planet(value).toColonyEntity();
                System.out.println("Collector has cached destination "+value+" and selected " + colonyEntity);
                return colonyEntity;
            }
            Planet planet = planetOptional.get();
            CacheEntryDAO.getInstance().setValue(COLLECTION_DESTINATION, planet.toString());
            ColonyEntity colonyEntity = planet.getSimilarColony();
            System.out.println("Collector see fleet flying to "+planet+" and selected " + colonyEntity);
            return colonyEntity;
        }
        ColonyEntity similarColony = configPlanet.getSimilarColony();
        System.out.println("Collector has destination "+configPlanet+" and selected " + similarColony);
        return similarColony;
    }

    private ColonyEntity anyColony() {
        long oneBeforeLast = Instance.getMainConfigMap().getConfigLong(ConfigMap.GALAXY_MAX, 6L)-1;
        ColonyEntity similarColony = new Planet("[" + oneBeforeLast + ":325:8]").getSimilarColony();
        System.out.println("Collector has no destination and selected " + similarColony.toString());
        return similarColony;
    }

    private boolean canItTransport(ColonyEntity colony) {
        return colony.transporterLarge >= colony.calculateTransportByTransporterLarge();
    }

    private long calculateResources(ColonyEntity colony) {
        if(colony == null) return 0L;
        else return colony.metal + colony.crystal*2 + colony.deuterium*3;
    }

    private boolean noCollectingByNavigator() {
        return Navigator.getInstance().noneMission(Mission.TRANSPORT);
    }
}
