package org.enoch.snark.instance;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.impl.PauseCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GICommandQueue {
    private static final Logger log = Logger.getLogger( GICommandQueue.class.getName() );

    private static final int SLEEP_PAUSE = 10;

    private Queue<AbstractCommand> prio = new LinkedList<>();
    private Queue<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private Instance instance;

    GICommandQueue(Instance instance) {

        this.instance = instance;
        startInterfaceQueue();
    }

    private void startInterfaceQueue() {
//        Runnable task = () -> {
//            while(true) {
//                if(fleetActionQueue.isEmpty()) {
//                    for(FleetEntity fleet : instance.daoFactory.fleetDAO.findToProcess()) {
//                        SendFleetCommand newSendFleet = new SendFleetCommand(instance, fleet);
//                        if(!containsFleetCommand(newSendFleet, fleetActionQueue)) {
//                            fleetActionQueue.add(newSendFleet);
//                        }
//                    }
//                }
//                if(!fleetActionQueue.isEmpty() && instance.commander.isFleetFreeSlot()) {
//                    resolve(fleetActionQueue.poll());
//                    fleetCount++;
//                    continue;
//                } else if(!interfaceActionQueue.isEmpty()) {
//                    resolve(interfaceActionQueue.poll());
//                    continue;
//                }
//
////                if(session.isLoggedIn())    session.close();
//                try {
//                    TimeUnit.SECONDS.sleep(SLEEP_PAUSE);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        new Thread(task).start();
    }

    private boolean containsFleetCommand(SendFleetCommand newSendFleet, Queue<AbstractCommand> fleetActionQueue) {
        return fleetActionQueue.stream().anyMatch(command -> command instanceof SendFleetCommand &&
                newSendFleet.mission.equals(((SendFleetCommand) command).mission) &&
                newSendFleet.fleet.target.planet.galaxy.equals(((SendFleetCommand) command).fleet.target.planet.galaxy) &&
                newSendFleet.fleet.target.planet.system.equals(((SendFleetCommand) command).fleet.target.planet.system) &&
                newSendFleet.fleet.target.planet.position.equals(((SendFleetCommand) command).fleet.target.planet.position));
    }
    private void resolve(AbstractCommand command) {
        boolean success;
        // każdy wyjątek powinien miec czy powtórzyć procedure
        // jesli nie ma tego to nalezy powtorzyc
        // poza tym wprost mozna zwrocic falsz czyli nie powtarzac
        //
        try {
            success = command.execute();
        }catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        if(success) {
            command.doAfter();
            log.info("Executed "+command+ " prepare "+ command.getAfterCommand());
        } else {
            command.failed++;
            if (command.failed < 5) {
                instance.commander.push(new PauseCommand(instance, command, 10));
            } else {
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
            }
        }
    }
}
