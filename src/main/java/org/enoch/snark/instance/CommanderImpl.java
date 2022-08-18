package org.enoch.snark.instance;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.impl.PauseCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommanderImpl implements Commander {

    private static final Logger log = Logger.getLogger(CommanderImpl.class.getName() );

    private static final int SLEEP_PAUSE = 5;
    public static final int TIME_TO_UPDATE = 5;

    private Instance instance;
    private GISession session;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusMinutes(TIME_TO_UPDATE);
    private boolean isRunning = true;

    private int fleetCount = 0;
    private int fleetMax = 0;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private Queue<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> calculationActionQueue = new LinkedList<>();

    private List<String> aggressorsAttacks = new ArrayList<>();

    public CommanderImpl(Instance instance) {
        this.instance = instance;
        this.session = instance.session;
        startInterfaceQueue();
        startCalculationQueue();
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private void startInterfaceQueue() {
        Runnable task = () -> {
            int tooManyFleetActions = 0;
            update();
            while(true) {
                //timeout after 5h

                if(instance.gi.webDriver.getCurrentUrl().contains("https://lobby.ogame.gameforge.com/pl_PL/hub")) {
                    this.isRunning = false;
                    instance.browserReset();
                    aggressorsAttacks = new ArrayList<>();
                    this.isRunning = true;
                }

                if (instance.isStopped()){
                    System.err.println("Is stopped");
                    this.isRunning = false;
                    try {
                        TimeUnit.SECONDS.sleep(SLEEP_PAUSE * 4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else {
                    this.isRunning = true;
                }

                if(false && isUnderAttack()) {
//                if(true) {
                    List<EventFleet> eventFleets = instance.gi.readEventFleet();
                    List<EventFleet> aggressors = eventFleets.stream()
                            .filter(eventFleet -> eventFleet.isForeign)
                            .collect(Collectors.toList());
                    aggressors.stream().forEach(
                            eventFleet -> {
                                if(!aggressorsAttacks.contains(eventFleet.arrivalTime)) {

                                    this.push(new SendMessageToPlayerCommand(instance, eventFleet.sendMail, "Bazinga!"));
                                    aggressorsAttacks.add(eventFleet.arrivalTime);
                                }
                            }
                    );
                }

                //todo to remove {
                if(fleetActionQueue.isEmpty()) {
                    for(FleetEntity fleet : instance.daoFactory.fleetDAO.findToProcess()) {
                        SendFleetCommand newSendFleet = new SendFleetCommand(instance, fleet);
                        if(!containsFleetCommand(newSendFleet, fleetActionQueue)) {
                            fleetActionQueue.add(newSendFleet);
                        }
                    }
                }
                // }
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

    private boolean isUnderAttack() {
        try {
            WebElement attack_alert = instance.gi.webDriver.findElement(By.id("attack_alert"));
            List<WebElement> soonElements = attack_alert.findElements(By.className("soon")); //todo classname nie łapie jesli jest tylko jedno wyspecyfikowane musi byc wszystkie
            if(!soonElements.isEmpty()) {
                log.warning("\nUnder Attack !! \n");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void update() {
        try {
            new GIUrlBuilder(instance).updateFleetStatus();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
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
