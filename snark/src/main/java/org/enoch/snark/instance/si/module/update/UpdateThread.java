package org.enoch.snark.instance.si.module.update;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.to.FleetMovement;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
public class UpdateThread extends AbstractThread {

    public static final String threadType = "update";
    public static final String REFRESH = "refresh";
    public static final String CHECK_SPY_REPORTS_DURATION = "check_spy_reports_duration";

    private final ColonyRepository colonyRepository;

    public Duration refresh = new Duration("12M");

    private Navigator navigator;
    private List<EventFleet> events;

    @Override
    protected void onStart() {
        super.onStart();
        navigator = Navigator.getInstance();
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return "1S";
    }

    @Override
    protected void onStep() {
        refresh.update(map.getConfig(REFRESH, "12M"));
        List<FleetMovement> pulled = Navigator.getInstance().pollExpired();
        pulled.forEach(fleetMovement -> {
            log(LocalDateTime.now() + " update after fleetMovement: "+fleetMovement);
        });
        updateColonies(pulled);

        if(isNavigatorExpired()) {
            updateState();
            log(LocalDateTime.now() + " state updated after "+refresh.getValue());
        }
        events = navigator.getEventFleetList();

        java.time.Duration duration = new Duration(getNearestConfig(CHECK_SPY_REPORTS_DURATION, "2M")).getValue();
        if(MessageService.getInstance().shouldTrigger(duration) && !alreadyWaiting(CHECK_SPY_REPORTS_DURATION)) {
            pushCommand(CHECK_SPY_REPORTS_DURATION, new ReadMessageCommand());
        }

        if (events == null) return;
        markSpecialFleets();
    }

    private void updateColonies(List<FleetMovement> movements) {
        movements.stream()
                .filter(FleetMovement::haveImpactOnColony)
                .map(movement -> THERE.equals(movement.getDirection()) ? movement.getTo() : movement.getFrom())
                .collect(Collectors.toSet())
                .stream().map(colonyRepository::byPlanet)
                .filter(Objects::nonNull)
                .forEach(colony -> pushCommand(new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName())));
    }

    private boolean isNavigatorExpired() {
        return  (Core.isSomethingAttacking && Navigator.getInstance().isExpiredAfter(new Duration("2M"))) ||
         navigator.isExpiredAfter(refresh);
    }

    public void updateState() {
        refreshCommand(new LoadColoniesCommand()
                .hash(threadType +"_LoadColoniesCommand")
                .setRunType(QueueRunType.MAJOR));
        refreshCommand(new UpdateFleetEventsCommand()
                .hash(threadType +"_UpdateFleetEventsCommand")
                .setRunType(QueueRunType.MAJOR));
    }

    private void markSpecialFleets() {
//        CacheEntryDAO cacheEntryDAO = CacheEntryDAO.getInstance();
//        markMainFleet();

//        ColonyEntity probeSwarmColony = ProbeSwarmFinder.find();
//        cacheEntryDAO.setValue(PROBE_SWAM, probeSwarmColony != null ? probeSwarmColony.toString() : null);
    }

//    private void markMainFleet() {
//        ShipsMap noProbe = new ShipsMap();
//        noProbe.put(Ship.espionageProbe, 0L);
//
//        List<Long> fleetCounts = ColonyDAO.getInstance().fetchAll().stream()
//                .map(colony -> colony.getShipsMap().reduce(noProbe).count())
//                .collect(Collectors.toList());
//        events.stream()
//                .filter(eventFleet -> !eventFleet.detailsFleet.trim().isEmpty())
//                .map(event -> NumberUtil.toLong(event.detailsFleet))
//                .collect(Collectors.toCollection(() -> fleetCounts));
//        OptionalLong max = fleetCounts.stream().mapToLong(value -> value).max();
//        if(max.isPresent()) {
//            return;
//        }
//    }
}
