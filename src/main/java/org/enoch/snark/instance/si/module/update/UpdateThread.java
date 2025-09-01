package org.enoch.snark.instance.si.module.update;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.action.command.LoadColoniesCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.model.to.FleetMovement;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
public class UpdateThread extends AbstractThread {

    public static final String threadType = "update";
    public static final String REFRESH = "refresh";

    private final ColonyRepository colonyRepository;

    public Duration refresh;

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
        refresh = map.getDuration(REFRESH, new Duration("12M"));
        boolean navigatorExpired = isNavigatorExpired();
        List<FleetMovement> pulled = Navigator.getInstance().pollExpired();
        pulled.forEach(fleetMovement -> {
            log(LocalDateTime.now() + " update after fleetMovement: "+fleetMovement);
        });
        updateColonies(pulled);
        if(navigatorExpired) {

            updateState();
            log(LocalDateTime.now() + " state updated");
        }


        events = navigator.getEventFleetList();
        if (events == null) return;
        markSpecialFleets();
    }

    private void updateColonies(List<FleetMovement> movements) {
        movements.stream()
                .filter(FleetMovement::haveImpactOnColony)
                .map(movement -> {
                    return  THERE.equals(movement.getDirection()) ? movement.getTo() : movement.getFrom();
//                    ColonyEntity colony = ColonyDAO.getInstance().find(toUpdate);
//                    ColonyEntity colony = colonyRepository.byPlanet(toUpdate);
//
//                    if(colony != null) {
//                        AbstractCommand abstractCommand = new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName());
//                        core.push(abstractCommand);
//                    }
                })
                .collect(Collectors.toSet())
                .stream().map(colonyRepository::byPlanet)
                .filter(Objects::nonNull)
                .forEach(colony -> core.push(new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName())));
    }

    private boolean isNavigatorExpired() {
        return  (Core.isSomethingAttacking && Navigator.getInstance().isExpiredAfter(new Duration("2M"))) ||
         navigator.isExpiredAfter(refresh);
    }

    public void updateState() {
        core.push(new LoadColoniesCommand()
                .hash(threadType +"_LoadColoniesCommand")
                .setRunType(QueueRunType.MAJOR));
        core.push(new UpdateFleetEventsCommand()
                .hash(threadType +"_UpdateFleetEventsCommand")
                .setRunType(QueueRunType.MAJOR));
    }

    private void markSpecialFleets() {
//        CacheEntryDAO cacheEntryDAO = CacheEntryDAO.getInstance();
//        markMainFleet();

//        ColonyEntity probeSwarmColony = ProbeSwarmFinder.find();
//        cacheEntryDAO.setValue(PROBE_SWAM, probeSwarmColony != null ? probeSwarmColony.toString() : null);
    }

    private void markMainFleet() {
        ShipsMap noProbe = new ShipsMap();
        noProbe.put(Ship.espionageProbe, 0L);

        List<Long> fleetCounts = ColonyDAO.getInstance().fetchAll().stream()
                .map(colony -> colony.getShipsMap().reduce(noProbe).count())
                .collect(Collectors.toList());
        events.stream()
                .filter(eventFleet -> !eventFleet.detailsFleet.trim().isEmpty())
                .map(event -> NumberUtil.toLong(event.detailsFleet))
                .collect(Collectors.toCollection(() -> fleetCounts));
        OptionalLong max = fleetCounts.stream().mapToLong(value -> value).max();
        if(max.isPresent()) {
            return;
        }
    }
}
