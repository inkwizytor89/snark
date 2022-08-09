package org.enoch.snark.instance;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.impl.PauseCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.exception.ShipDoNotExists;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CommanderImpl implements Commander {

    private static final Logger log = Logger.getLogger( CommanderImpl.class.getName() );

    private static final int SLEEP_PAUSE = 5;
    public static final int TIME_TO_UPDATE = 5;

    private Instance instance;
    private GISession session;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(TIME_TO_UPDATE);

    private int fleetCount = 0;
    private int fleetMax = 0;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private Queue<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> calculationActionQueue = new LinkedList<>();

    public CommanderImpl(Instance instance) {
        this.instance = instance;
        this.session = instance.session;
        startInterfaceQueue();
        startCalculationQueue();
    }

    private void startInterfaceQueue() {
        Runnable task = () -> {
            int tooManyFleetActions = 0;
            while(true) {
                if(fleetActionQueue.isEmpty()) {
                    for(FleetEntity fleet : instance.daoFactory.fleetDAO.findToProcess()) {
                        SendFleetCommand newSendFleet = new SendFleetCommand(instance, fleet);
                        if(!containsFleetCommand(newSendFleet, fleetActionQueue)) {
                            fleetActionQueue.add(newSendFleet);
                        }
                    }
                }
                if(!fleetActionQueue.isEmpty() && isFleetFreeSlot() && tooManyFleetActions < 8) {
                    resolve(fleetActionQueue.poll());
                    fleetCount++;
                    tooManyFleetActions++;
                    update();
                    continue;
                } else if(!interfaceActionQueue.isEmpty()) {
                    resolve(interfaceActionQueue.poll());
                    tooManyFleetActions = 0;
                    continue;
                }
                tooManyFleetActions = 0;

//                if(session.isLoggedIn())    session.close();
                if(LocalDateTime.now().isAfter(lastUpdate.plusMinutes(TIME_TO_UPDATE))) {
                    update();
                }
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_PAUSE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    private void update() {
        new GIUrlBuilder(instance).updateFleetStatus();
    }

    private boolean containsFleetCommand(SendFleetCommand newSendFleet, Queue<AbstractCommand> fleetActionQueue) {
        return fleetActionQueue.stream().anyMatch(command -> command instanceof SendFleetCommand &&
                newSendFleet.mission.equals(((SendFleetCommand) command).mission) &&
                newSendFleet.fleet.targetGalaxy.equals(((SendFleetCommand) command).fleet.targetGalaxy) &&
                newSendFleet.fleet.targetSystem.equals(((SendFleetCommand) command).fleet.targetSystem) &&
                newSendFleet.fleet.targetPosition.equals(((SendFleetCommand) command).fleet.targetPosition));
    }

    private void startCalculationQueue() {
        Runnable task = () -> {
            while(true) {

                while(!calculationActionQueue.isEmpty()) {
                    resolve(calculationActionQueue.poll());
                }
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_PAUSE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    private void resolve(AbstractCommand command) {
        boolean success;
        if(command.requiredGI() && !session.isLoggedIn()) {
            session.open();
        }
        // każdy wyjątek powinien miec czy powtórzyć procedure
        // jesli nie ma tego to nalezy powtorzyc
        // poza tym wprost mozna zwrocic falsz czyli nie powtarzac
        //
        try {
            success = command.execute();
        } catch (ShipDoNotExists e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            success = false;
        }

        if(success) {
            command.doAfter();
            log.info("Executed "+command+ " prepare "+ command.getAfterCommand());
        } else {
            command.failed++;
            if (command.failed < 3) {
                push(new PauseCommand(instance, command, 2));
            } else {
                command.onInterrupt();
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
            }
        }
    }

    private boolean isFleetFreeSlot() {
        if(!session.isLoggedIn()) return false;
        if(getFleetFreeSlots() > 0)   return true;
        new GIUrlBuilder(instance).updateFleetStatus();
        if(getFleetFreeSlots() > 0)   return true;
        return false;
    }

    public void setFleetStatus(int fleetCount, int fleetMax) {
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
        lastUpdate = LocalDateTime.now();
    }

    @Override
    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
        lastUpdate = LocalDateTime.now();
    }

    public synchronized void push(AbstractCommand command) {
        if (CommandType.FLEET_REQUIERED.equals(command.getType())) {
            fleetActionQueue.offer(command);
            log.info("Inserted "+command+" into queue fleetActionQueue size "+fleetActionQueue.size());
        } else if (CommandType.INTERFACE_REQUIERED.equals(command.getType())) {
            interfaceActionQueue.offer(command);
            log.info("Inserted "+command+" into queue interfaceActionQueue size "+interfaceActionQueue.size());
        } else if (CommandType.CALCULATION.equals(command.getType())) {
            calculationActionQueue.offer(command);
            log.info("Inserted "+command+" into queue calculationActionQueue size "+calculationActionQueue.size());
        }else {
            throw new RuntimeException("Invalid type of command");
        }
    }

    public List<AbstractCommand> peekFleetQueue() {
        return (LinkedList<AbstractCommand>)((LinkedList<AbstractCommand>) fleetActionQueue).clone();
    }

    public int getFleetFreeSlots() {
        return fleetMax - fleetCount;
    }

    @Override
    public int getExpeditionFreeSlots() {
        return expeditionMax - expeditionCount;
    }

    public int getFleetCount() {
        return fleetCount;
    }

    public int getFleetMax() {
        return fleetMax;
    }

    public int getExpeditionCount() {
        return expeditionCount;
    }

    public int getExpeditionMax() {
        return expeditionMax;
    }
}
