package org.enoch.snark.model.service;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ReadMessageCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.common.SleepUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.model.types.MissionType.SPY;

public class MessageService {
    private static MessageService INSTANCE;

    private List<LocalDateTime> cache = new ArrayList<>();
    private LocalDateTime lastChecked = LocalDateTime.now();
    public static final Object sync = new Object();

    private MessageService() {
        Runnable task = () -> {
            while(true) {
                List<LocalDateTime> enoughWaiting = new ArrayList<>();
                synchronized (sync) {
                    for(LocalDateTime date : cache) {
                        if(date.isBefore(LocalDateTime.now())){
                            enoughWaiting.add(date);
                        }
                    }
                    for(LocalDateTime date : enoughWaiting) {
                        cache.remove(date);
                    }
                }
                if(!enoughWaiting.isEmpty()) {
                    Instance.getInstance().push(new ReadMessageCommand());
                }
                SleepUtil.secondsToSleep(60);
//                // reduce fleet table
//                Optional<FleetEntity> first = FleetDAO.getInstance().fetchAll().stream()
//                        .filter(fleetEntity -> SPY.getName().equals(fleetEntity.type))
//                        .filter(fleetEntity -> fleetEntity.updated.isAfter(lastChecked))
//                        .sorted(Comparator.comparing(o -> o.updated))
//                        .limit(30)
//                        .max(Comparator.comparing(o -> o.updated));
//                if(first.isPresent()) {
//                    FleetEntity spyToCheck = first.get();
//                    if(spyToCheck.visited != null && LocalDateTime.now().plusSeconds(3).isAfter(spyToCheck.visited)) {
//                        Instance.getInstance().push(new ReadMessageCommand());
//                    }
//                }
//                SleepUtil.secondsToSleep(20);
            }
        };

        new Thread(task).start();
    }

    public static MessageService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MessageService();
        }
        return INSTANCE;
    }

    public void put(int secondToCheck) {
        synchronized (sync) {
            cache.add(LocalDateTime.now().plusSeconds(secondToCheck));
        }
    }

}
