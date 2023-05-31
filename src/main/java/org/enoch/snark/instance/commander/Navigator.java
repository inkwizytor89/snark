package org.enoch.snark.instance.commander;

import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.model.EventFleet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Navigator {

    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);

    private Navigator() {
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public boolean isExpiredAfterMinutes(int minutes) {
        return lastUpdate.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        int fleetCount = Commander.getInstance().getFleetCount();
        if(eventFleetList == null || eventFleetList.size() < fleetCount) {
            System.err.println("informAboutEventFleets null");
            return;
        }
        this.eventFleetList = eventFleetList;
        System.err.println("informAboutEventFleets size "+this.eventFleetList.size());

        this.lastUpdate = LocalDateTime.now();
    }

    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    public boolean noneMission(Mission mission) {
        return this.eventFleetList.stream()
                .noneMatch(fleet -> mission.equals(fleet.mission));
    }

    @Override
    public String toString() {
        return eventFleetList.stream().map(event -> event + "\n")
                .collect(Collectors.joining("", "Navigator :\n", ""));
    }
}
