package org.enoch.snark.instance.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ReadMessageCommand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.types.Mission.SPY;

public class MessageService {
    private static MessageService INSTANCE;

    private LocalDateTime lastChecked = LocalDateTime.now();

    private MessageService() {
        Runnable task = () -> {
            while(true) {

                if(isTimeForReadMessages()) {
                    new ReadMessageCommand().hash(MessageService.class.getName()).push();
                }
                SleepUtil.secondsToSleep(10L);

//                List<FleetEntity> notLoadedSpyActions = FleetDAO.getInstance().findLastSend(lastChecked).stream()
//                        .filter(fleet -> SPY.getName().equals(fleet.type))
//                        .collect(Collectors.toList());
//
//                long readySpyReports = notLoadedSpyActions.stream()
//                        .filter(fleet -> fleet.visited != null && fleet.visited.isAfter(LocalDateTime.now()))
//                        .count();
//
//                long waitingSpyReports = notLoadedSpyActions.size() - readySpyReports;
//                if(notLoadedSpyActions.size() > 0 && (waitingSpyReports == 0 || readySpyReports > 35L)) {
//                    Instance.getInstance().push(new ReadMessageCommand());
                }
//            }
        };

        new Thread(task).start();
    }

    private boolean isTimeForReadMessages() {
        List<FleetEntity> notLoadedSpyActions = FleetDAO.getInstance().findLastSend(lastChecked).stream()
                .filter(fleet -> SPY.equals(fleet.mission))
                .collect(Collectors.toList());

        Multimap<Long, FleetEntity> waitingMap = ArrayListMultimap.create();
        Multimap<Long, FleetEntity> scannedMap = ArrayListMultimap.create();

        notLoadedSpyActions.forEach(fleet -> {
            if(fleet.visited == null || LocalDateTime.now().isBefore(fleet.visited)) {
                waitingMap.put(fleet.code, fleet);
            } else {
                scannedMap.put(fleet.code, fleet);
            }
        });
//
//
//        long waiting = notLoadedSpyActions.stream()
//                .filter(fleet -> fleet.visited == null ||
//                        LocalDateTime.now().plusSeconds(10).isBefore(fleet.visited))
//                .count();
//        long scannedTargets = notLoadedSpyActions.size() - waiting;

        // some actions are ended and is no time to wait more
        for (Long key : scannedMap.keySet()) {
            if(key != null && waitingMap.get(key).isEmpty()) {
                System.err.println("For "+key+" is time to read messages");
                return true;
            }
        }

        return notLoadedSpyActions.size() > 0 && (waitingMap.values().size() == 0 || scannedMap.values().size() > 20L);
    }

    public static MessageService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MessageService();
        }
        return INSTANCE;
    }

    public void update() {
        lastChecked = LocalDateTime.now();
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

}
