package org.enoch.snark.instance.si;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.common.status.ColonyStatus;
import org.enoch.common.status.CoordinatesStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.common.status.StatusDetails;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.technology.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.enoch.snark.instance.si.module.AbstractThread.PROCESSING_SUFFIX;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;

@Service
@RequiredArgsConstructor
public class Core {

    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;

    private CommandDeque commandDeque;
    private boolean isDequeReady;
    public static boolean isSomethingAttacking;

    @Getter
    @Setter
    private static ColonyEntity lastVisited;

    @PostConstruct
    public void init() {
        cacheEntryRepository.setUnknownForSuffix(PROCESSING_SUFFIX);
    }

    public void register(CommandDeque commandDeque) {
        this.commandDeque = commandDeque;
        System.err.println("Register CommandDeque");
    }

    public synchronized boolean isDequeReady() {
        if(isDequeReady) return true;
        isDequeReady = commandDeque != null && commandDeque.isEmpty();
        return isDequeReady;
    }

    public void push(AbstractCommand command) {
        if(commandDeque == null)
            System.err.println("Unexpected error: "+command);
        commandDeque.push(command);
    }

    public ServerStatus status() {
        return buildStatus(true);
    }

    public ServerStatus statusSummary() {
        return buildStatus(false);
    }

    public StatusDetails statusDetails(int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 25) {
            throw new IllegalArgumentException("Status details require offset >= 0 and limit between 1 and 25");
        }
        List<ColonyEntity> colonies = sortedColonies();
        int planetCount = (int) colonies.stream().filter(colony -> colony.is(PLANET)).count();
        int moonCount = colonies.size() - planetCount;
        int fromIndex = Math.min(offset, colonies.size());
        int toIndex = Math.min(fromIndex + limit, colonies.size());
        List<ColonyStatus> page = colonies.subList(fromIndex, toIndex).stream()
                .map(this::toColonyStatus)
                .toList();
        return new StatusDetails(offset, limit, colonies.size(), planetCount, moonCount, page);
    }

    private ServerStatus buildStatus(boolean includeColonies) {
        List<String> activeThreads = new ArrayList<>();
        List<String> notifications = new ArrayList<>();
        CoordinatesStatus coordinatesStatus = null;
        if (includeColonies) {
            List<ColonyEntity> colonies = sortedColonies();
            int planetCount = (int) colonies.stream().filter(colony -> colony.is(PLANET)).count();
            int moonCount = colonies.size() - planetCount;
            List<ColonyStatus> colonyStatuses = colonies.stream().map(this::toColonyStatus).toList();
            coordinatesStatus = new CoordinatesStatus(planetCount, moonCount, colonyStatuses);
        }
        var navigatorStatus = Navigator.getInstance().getStatus();
        List<String> activeThreadSummary = activeThreads;
        List<String> notificationSummary = notifications;
        if (!includeColonies) {
            navigatorStatus = new org.enoch.common.status.NavigatorStatus(
                    navigatorStatus.getFleetCount(),
                    navigatorStatus.getFleetMax(),
                    navigatorStatus.getExpeditionCount(),
                    navigatorStatus.getExpeditionMax(),
                    navigatorStatus.getMissions(),
                    navigatorStatus.getLastUpdate(),
                    truncate(navigatorStatus.getFleetStatus(), 256));
            activeThreadSummary = activeThreads.stream()
                    .limit(32)
                    .map(thread -> truncate(thread, 40))
                    .toList();
            notificationSummary = notifications.stream().limit(20).toList();
        }
        String notificationText = String.join(System.lineSeparator() + System.lineSeparator(), notificationSummary);
        String commandDequeText = commandDeque == null ? "" : commandDeque.toString();
        if (!includeColonies) {
            notificationText = truncate(notificationText, 512);
            commandDequeText = truncate(commandDequeText, 256);
        }
        return new ServerStatus(
                navigatorStatus,
                activeThreadSummary,
                commandDequeText,
                notificationText,
                coordinatesStatus
        );
    }

    private List<ColonyEntity> sortedColonies() {
        return colonyRepository.findAll().stream()
                .sorted(Comparator.comparing(colony -> colony.toPlanet().toString()))
                .toList();
    }

    private ColonyStatus toColonyStatus(ColonyEntity colony) {
        Map<String, Long> ships = new LinkedHashMap<>();
        addShip(ships, Ship.fighterLight, colony.fighterLight);
        addShip(ships, Ship.fighterHeavy, colony.fighterHeavy);
        addShip(ships, Ship.cruiser, colony.cruiser);
        addShip(ships, Ship.battleship, colony.battleship);
        addShip(ships, Ship.interceptor, colony.interceptor);
        addShip(ships, Ship.bomber, colony.bomber);
        addShip(ships, Ship.destroyer, colony.destroyer);
        addShip(ships, Ship.deathstar, colony.deathstar);
        addShip(ships, Ship.reaper, colony.reaper);
        addShip(ships, Ship.explorer, colony.explorer);
        addShip(ships, Ship.transporterSmall, colony.transporterSmall);
        addShip(ships, Ship.transporterLarge, colony.transporterLarge);
        addShip(ships, Ship.colonyShip, colony.colonyShip);
        addShip(ships, Ship.recycler, colony.recycler);
        addShip(ships, Ship.espionageProbe, colony.espionageProbe);
        addShip(ships, Ship.solarSatellite, colony.solarSatellite);
        return new ColonyStatus(
                colony.toPlanet().toString(),
                colony.metal,
                colony.crystal,
                colony.deuterium,
                colony.energy,
                ships
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private void addShip(Map<String, Long> ships, Ship ship, Long count) {
        if (count != null && count > 0) {
            ships.put(ship.name(), count);
        }
    }

//    public synchronized void push(AbstractCommand command, String action) {
//        if (!fleetRepository.isBlockedWithExpiredTime(command.getHash(), action)) push(command);
//    }
}
