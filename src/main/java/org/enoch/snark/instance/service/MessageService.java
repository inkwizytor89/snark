package org.enoch.snark.instance.service;

import lombok.Getter;
import org.enoch.snark.instance.model.to.Planet;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MessageService {
    private static MessageService INSTANCE;

    private Map<Planet, LocalDateTime> waiting = new HashMap<>();

    @Getter
    private LocalDateTime lastChecked = LocalDateTime.now();

    public void put(Planet planet) {
        waiting.put(planet, LocalDateTime.now());
    }

    public void release(Planet planet) {
        waiting.remove(planet);
        System.err.println("MessageService.release "+planet+" left "+waiting.size());
    }

    public static MessageService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MessageService();
        }
        return INSTANCE;
    }

    public boolean shouldTrigger(Duration maxWait) {
        if (waiting.size() >= 30) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        for (LocalDateTime time : waiting.values()) {
            if (Duration.between(time, now).compareTo(maxWait) > 0) {
                return true;
            }
        }

        return false;
    }

}
