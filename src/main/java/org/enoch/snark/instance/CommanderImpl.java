package org.enoch.snark.instance;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.impl.PauseCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class CommanderImpl implements Commander {

    private static final Logger log = Logger.getLogger(CommanderImpl.class.getName() );

    private static final long SLEEP_PAUSE = 5;
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

    private List<String> aggressorsAttacks = new ArrayList<>();

    public CommanderImpl(Instance instance) {
        this.instance = instance;
        this.session = instance.session;
        startInterfaceQueue();
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private void startInterfaceQueue() {
        Runnable task = () -> {
            update();// shold be load game status responsibility
            while(true) {
                try {
                    restartIfSessionIsOver();

                    if (instance.isStopped()) {
                        stopCommander();
                        continue;
                    }

                    startCommander();
                    activateDefenseIfNeeded();

                    if (!fleetActionQueue.isEmpty() && isFleetFreeSlot()) {
                        resolve(fleetActionQueue.poll());
                        fleetCount++;
                        update();
                        continue;
                    } else if (!interfaceActionQueue.isEmpty()) {
                        resolve(interfaceActionQueue.poll());
                        continue;
                    }

                    if (LocalDateTime.now().isAfter(lastUpdate.plusMinutes(TIME_TO_UPDATE))) {
                        update();
                    }

                    Utils.secondsToSleep(SLEEP_PAUSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    private void activateDefenseIfNeeded() {
        if(isUnderAttack()) {
            instance.gi.readEventFleet().stream()
                    .filter(eventFleet -> eventFleet.isForeign)
                    // ss scans
                    .filter(eventFleet -> LocalTime.now().plusSeconds(120).isBefore(DateUtil.parseTime(eventFleet.arrivalTime)))
                    .collect(Collectors.toList())
                    .forEach(
                    eventFleet -> {
                        if(!aggressorsAttacks.contains(eventFleet.arrivalTime)) {
                            log.warning(instance.getName()+" aggressor found "+eventFleet.toString());
                            this.push(new SendMessageToPlayerCommand(instance, eventFleet.sendMail, Msg.get(BAZINGA_PL)));
                            aggressorsAttacks.add(eventFleet.arrivalTime);
                        }
                    }
            );
        }
    }

    private void startCommander() {
        this.isRunning = true;
    }

    private void stopCommander() {
        System.err.println(instance.getName() + " is stopped");
        this.isRunning = false;
        Utils.secondsToSleep(SLEEP_PAUSE * 4);
    }

    private void restartIfSessionIsOver() {
        if(instance.gi.webDriver.getCurrentUrl().contains("https://lobby.ogame.gameforge.com/")) {
            stopCommander();
            instance.browserReset();
            aggressorsAttacks = new ArrayList<>();
            startCommander();
        }
    }

    private boolean isUnderAttack() {
        try {
            WebElement attack_alert = instance.gi.webDriver.findElement(By.id("attack_alert"));
            if(attack_alert.getAttribute("class").contains("soon")) {
                log.warning("\n"+instance.getName() + " Under Attack !! \n");
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
            log.info(instance.getName()+ " executed "+command+ " prepare "+ command.getAfterCommand());
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
//            log.info("Inserted "+command+" into queue fleetActionQueue size "+fleetActionQueue.size());
        } else if (CommandType.INTERFACE_REQUIERED.equals(command.getType())) {
            interfaceActionQueue.offer(command);
//            log.info("Inserted "+command+" into queue interfaceActionQueue size "+interfaceActionQueue.size());
        } else {
            throw new RuntimeException("Invalid type of command");
        }
    }

    public synchronized List<AbstractCommand> peekFleetQueue() {
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
