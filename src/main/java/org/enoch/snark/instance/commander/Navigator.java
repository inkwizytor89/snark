package org.enoch.snark.instance.commander;

import org.enoch.snark.model.EventFleet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Navigator {

    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList = new ArrayList<>();
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);

    private Navigator() {
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        if(eventFleetList == null) {
            System.err.println("Navigator loaded null data - skipping");
            return;
        }
        this.eventFleetList = eventFleetList;
        this.lastUpdate = LocalDateTime.now();
    }

    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    @Override
    public String toString() {
        return eventFleetList.stream().map(event -> event + "\n")
                .collect(Collectors.joining("", "Navigator :\n", ""));
    }
}
