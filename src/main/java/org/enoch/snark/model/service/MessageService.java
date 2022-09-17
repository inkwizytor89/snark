package org.enoch.snark.model.service;

import org.enoch.snark.gi.command.impl.ReadMessageCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.common.SleepUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private static MessageService INSTANCE;

    private List<LocalDateTime> cache = new ArrayList<>();
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
