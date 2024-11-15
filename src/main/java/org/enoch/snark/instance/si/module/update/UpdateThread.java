package org.enoch.snark.instance.si.module.update;

import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.gi.command.impl.UpdateFleetEventsCommand;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.find.ProbeSwarmFinder;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.action.PlanetExpression.PROBE_SWAM;

public class UpdateThread extends AbstractThread {

    public static final String threadType = "update";
    public static final String REFRESH = "refresh";
    public int updateTimeInMinutes = 12;
    private int threadPause = 10;

    private Navigator navigator;
    private List<EventFleet> events;

    public UpdateThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigator = Navigator.getInstance();
        threadPause = 10;
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return threadPause;
    }

    @Override
    protected void onStep() {
        updateTimeInMinutes = map.getConfigInteger(REFRESH, 12);
        boolean navigatorExpired = isNavigatorExpired();
        boolean b = commander.noBlockingHashInQueue(threadType);
        log(LocalDateTime.now() + " update check: navigatorExpired="+navigatorExpired+" noBlockingHashInQueue="+b);
        if(navigatorExpired && b) {
            updateState();
            log(LocalDateTime.now() + " state updated");
        }

        events = navigator.getEventFleetList();
        if (events == null) return;
        markSpecialFleets();
    }

    private boolean isNavigatorExpired() {
        return navigator.isExpiredAfterMinutes(updateTimeInMinutes);
    }

    public static void updateState() {
        new LoadColoniesCommand()
                .hash(threadType +"_LoadColoniesCommand")
                .setRunType(QueueRunType.MAJOR)
                .push();
        new UpdateFleetEventsCommand()
                .hash(threadType +"_UpdateFleetEventsCommand")
                .setRunType(QueueRunType.MAJOR)
                .push();
    }

    private void markSpecialFleets() {
        CacheEntryDAO cacheEntryDAO = CacheEntryDAO.getInstance();
//        markMainFleet();

        ColonyEntity probeSwarmColony = ProbeSwarmFinder.find();
        cacheEntryDAO.setValue(PROBE_SWAM, probeSwarmColony != null ? probeSwarmColony.toString() : null);
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
