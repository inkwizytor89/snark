package org.enoch.snark.instance;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class GICommandQueue {

    private Queue<AbstractCommand> prio = new LinkedList<>();
    private Queue<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private Instance instance;

    GICommandQueue(Instance instance) {

        this.instance = instance;
    }

    private void startInterfaceQueue() {
        Runnable task = () -> {
            while(true) {
                if(fleetActionQueue.isEmpty()) {
                    for(FleetEntity fleet : instance.daoFactory.fleetDAO.findToProcess()) {
                        SendFleetCommand newSendFleet = new SendFleetCommand(instance, fleet);
                        if(!containsFleetCommand(newSendFleet, fleetActionQueue)) {
                            fleetActionQueue.add(newSendFleet);
                        }
                    }
                }
                if(!fleetActionQueue.isEmpty() && isFleetFreeSlot()) {
                    resolve(fleetActionQueue.poll());
                    fleetCount++;
                    continue;
                } else if(!interfaceActionQueue.isEmpty()) {
                    resolve(interfaceActionQueue.poll());
                    continue;
                }

//                if(session.isLoggedIn())    session.close();
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_PAUSE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }
}
