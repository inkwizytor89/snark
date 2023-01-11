package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.gi.command.impl.CommandType;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class Commander {

    private static final int SLEEP_PAUSE = 1;
    private static Commander INSTANCE;

    private final Instance instance;
    private final GISession session;
    private boolean isRunning = true;
    private boolean isUnderAttack = false;

    private int fleetCount = 0;
    private int fleetMax = 0;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private final Queue<AbstractCommand> priorityActionQueue = new LinkedList<>();
    private final Queue<AbstractCommand> normalActionQueue = new LinkedList<>();
    private AbstractCommand actualProcessedCommand = null;

    public Commander() {
        this.instance = Instance.getInstance();
        this.session = Instance.session;
        startInterfaceQueue();
    }

    public static Commander getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Commander();
        }
        return INSTANCE;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    void startInterfaceQueue() {
        Runnable task = () -> {
            checkFlyPoints();
            while(true) {
                try {
                    if(!isRunning) continue;

                    restartIfSessionIsOver();

                    if (instance.isStopped()) {
                        stopCommander();
                        continue;
                    }

                    startCommander();

//                    if(isSomethingAttacking()) {
//                        if(!isUnderAttack) resolve(new OpenPageCommand(PAGE_BASE_FLEET, null)
//                                .setCheckEventFleet(true));
//                        isUnderAttack = true;
//                    } else isUnderAttack = false;

                    if(isFleetFreeSlot()) {
                        if (!priorityActionQueue.isEmpty()) {
                            resolve(Objects.requireNonNull(priorityActionQueue.poll()));
                            fleetCount++;
                            SleepUtil.sleep();
                            continue;
                        }
                    }
                    if (!normalActionQueue.isEmpty()) {
                            resolve(normalActionQueue.poll());
                            continue;
                    }
                    if(isFleetFreeSlot()) {
                        List<FleetEntity> toProcess = FleetDAO.getInstance().findToProcess();
                        if (!toProcess.isEmpty()) {
                            FleetEntity waitingFleet = toProcess.get(0);
//                                System.err.println("Find waiting fleet "+waitingFleet);
                            resolve(new SendFleetCommand(waitingFleet));
                            SleepUtil.sleep();
                            fleetCount++;
                            continue;
                        }
                    }
                    SleepUtil.secondsToSleep(SLEEP_PAUSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = instance.gi.webDriver.findElement(By.id("attack_alert"));
            if(attack_alert.getAttribute("class").contains("soon")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkFlyPoints() {
        for(ColonyEntity colony : instance.flyPoints) {
            push(new OpenPageCommand(PAGE_BASE_FLEET, colony));
        }
    }

    private void startCommander() {
        this.isRunning = true;
    }

    private void stopCommander() {
        System.err.println("Commander is stopped");
        this.isRunning = false;
        SleepUtil.secondsToSleep(SLEEP_PAUSE * 30);
    }

    private void restartIfSessionIsOver() {
        try {
            if (instance.gi.webDriver.getCurrentUrl().contains("https://lobby.ogame.gameforge.com/")) {
                stopCommander();
                System.err.println("sleep 300 before restart");
                SleepUtil.secondsToSleep(300);
                instance.browserReset();
                startCommander();
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
//            stopCommander();
//            System.err.println("sleep 500 before restart");
//            SleepUtil.secondsToSleep(500);
//            instance.browserReset();
//            startCommander();
        }
    }

    private synchronized void resolve(AbstractCommand command) {
        actualProcessedCommand = command;
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
            String commandMessage = command.toString();
//            log.info(command.toString());
            if(command.isAfterCommand()) {
//                log.info("Next move in "+ command.secondsToDelay+"s is "+ command.getAfterCommand());
                command.doAfter();
            }
        } else {
            command.failed++;
            if (command.failed < 2) {
                command.retry(2);
            } else {
                command.onInterrupt();
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
            }
        }
        actualProcessedCommand = null;
    }

    private boolean isFleetFreeSlot() {
        if(!session.isLoggedIn()) return false;
        if(getFleetFreeSlots() > 0)   return true;
        return false;
    }

    public void setFleetStatus(int fleetCount, int fleetMax) {
//        System.err.println("Fleet status "+fleetCount+"/"+fleetMax);
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
    }

    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
    }

    public synchronized void push(AbstractCommand command) {
        if (CommandType.PRIORITY_REQUIERED.equals(command.getType())) {
            priorityActionQueue.offer(command);
        } else if (CommandType.NORMAL_REQUIERED.equals(command.getType())) {
            normalActionQueue.offer(command);
        } else {
            throw new RuntimeException("Invalid type of command");
        }
    }

    public synchronized List<AbstractCommand> peekQueues() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        if (actualProcessedCommand != null) commandsToView.add(actualProcessedCommand);
        commandsToView.addAll(priorityActionQueue);
        commandsToView.addAll(normalActionQueue);
        return commandsToView;
    }

    @Deprecated
    public synchronized AbstractCommand getActualProcessedCommand() {
        return actualProcessedCommand;
    }

    public int getFleetFreeSlots() {
        return fleetMax - fleetCount;
    }

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
