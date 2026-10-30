package org.enoch.common.status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavigatorStatus {

    private Integer fleetCount;
    private Integer fleetMax;
    private Integer expeditionCount;
    private Integer expeditionMax;
    private Map<String, Long> missions;
    private LocalDateTime lastUpdate;
    private String fleetStatus;

    public NavigatorStatus(String fleetStatus, LocalDateTime lastUpdate) {
        this.fleetStatus = fleetStatus;
        this.lastUpdate = lastUpdate;
    }

    public NavigatorStatus(int fleetCount, int fleetMax, int expeditionCount, int expeditionMax,
                          Map<String, Long> missions, LocalDateTime lastUpdate) {
        this(fleetCount, fleetMax, expeditionCount, expeditionMax, missions, lastUpdate, null);
    }

    public NavigatorStatus(int fleetCount, int fleetMax, int expeditionCount, int expeditionMax,
                          Map<String, Long> missions, LocalDateTime lastUpdate, String fleetStatus) {
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
        this.missions = missions == null ? new LinkedHashMap<>() : new LinkedHashMap<>(missions);
        this.lastUpdate = lastUpdate;
        this.fleetStatus = fleetStatus != null ? fleetStatus : renderFleetStatus();
    }

    @JsonIgnore
    public String renderFleetStatus() {
        String counts = (fleetCount == null ? "0" : fleetCount)
                + " / " + (fleetMax == null ? "0" : fleetMax)
                + " " + (expeditionCount == null ? "0" : expeditionCount)
                + " / " + (expeditionMax == null ? "0" : expeditionMax);
        if (missions == null || missions.isEmpty()) {
            return counts;
        }
        String missionSummary = missions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().substring(0, 1) + ":" + entry.getValue())
                .collect(Collectors.joining(" ", " ", ""));
        return counts + missionSummary;
    }

    public String getFleetStatus() {
        if (fleetStatus != null) {
            return fleetStatus;
        }
        return renderFleetStatus();
    }

    public void setFleetStatus(String fleetStatus) {
        this.fleetStatus = fleetStatus;
    }
}
