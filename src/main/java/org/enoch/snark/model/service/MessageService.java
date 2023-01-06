package org.enoch.snark.model.service;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.ReadMessageCommand;
import org.enoch.snark.instance.Instance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.model.types.MissionType.SPY;

public class MessageService {
    private static MessageService INSTANCE;

    private LocalDateTime lastChecked = LocalDateTime.now();

    private MessageService() {
        Runnable task = () -> {
            while(true) {
                List<FleetEntity> lastSpy = FleetDAO.getInstance().findLastSend(lastChecked).stream()
                        .filter(fleet -> SPY.getName().equals(fleet.type))
                        .collect(Collectors.toList());

                long waiting = lastSpy.stream()
                        .filter(fleet -> fleet.visited == null ||
                                LocalDateTime.now().plusSeconds(5).isBefore(fleet.visited))
                        .count();
                long scannedTargets = lastSpy.size() - waiting;
                if(lastSpy.size() > 0 && (waiting == 0 || scannedTargets > 40L)) {
                    Instance.getInstance().push(new ReadMessageCommand());
                }
                SleepUtil.secondsToSleep(10);
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

    public void update() {
        lastChecked = LocalDateTime.now();
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

}
