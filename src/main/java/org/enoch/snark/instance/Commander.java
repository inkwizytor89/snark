package org.enoch.snark.instance;

import org.enoch.snark.common.RunningStatus;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;
import static org.enoch.snark.instance.config.Config.MODE;
import static org.enoch.snark.instance.config.Config.STOP;

public class Commander extends Thread {

    private static final int SLEEP_PAUSE = 1;
    public static final String LOBBY_URL = "https://lobby.ogame.gameforge.com/";
    private static Commander INSTANCE;

    private final Instance instance;
    private final GISession session;
    private boolean isRunning = true;

    private int fleetCount = 0;
    private int fleetMax = 1;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private final Deque<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private final Deque<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private AbstractCommand actualProcessedCommand = null;

    public Commander() {
        this.instance = Instance.getInstance();
        this.session = Instance.session;
        start();
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

    @Override
    public void run() {
        waitingToOpenServerTab();
        while(true) {
            Instance.updateConfig();
            try {
                RunningStatus runningStatus = createRunningStatus();
                isRunning = createRunningStatus().shouldRunning();
                runningStatus.log(Commander.class.getName());
                if(!runningStatus.shouldRunning()) continue;

                restartIfSessionIsOver();

                if (instance.isStopped()) {
                    stopCommander();
                    continue;
                }

                startCommander();

                if(isSomethingAttacking() && Navigator.getInstance().isExpiredAfterMinutes(2)) {
                    resolve(new OpenPageCommand(PAGE_BASE_FLEET).setCheckEventFleet(true));
                }

                // check if fleet slot is free because is something fleet to send
                if(!isFleetFreeSlot() && !fleetActionQueue.isEmpty()) {
                    resolve(new OpenPageCommand(PAGE_BASE_FLEET));
                }
                // method for put top in fleet queue
                if(isFleetFreeSlot()) {
                    if (!fleetActionQueue.isEmpty()) {
                        resolve(Objects.requireNonNull(fleetActionQueue.poll()));
                        SleepUtil.sleep();
                        continue;
                    }
                }
                if (!interfaceActionQueue.isEmpty()) {
                        resolve(interfaceActionQueue.poll());
                        continue;
                }
                if(isFleetFreeSlot()) {
                    List<FleetEntity> toProcess = FleetDAO.getInstance().findToProcess();
                    if (!toProcess.isEmpty()) {
                        resolve(new SendFleetCommand(toProcess.get(0)));
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
    }

    private void waitingToOpenServerTab() {
        while (instance.gi.webDriver.getCurrentUrl().contains(LOBBY_URL)) {
            SleepUtil.pause();
        }
    }

    public RunningStatus createRunningStatus() {
        boolean stopRunning = Instance.config.getConfig(MODE).toLowerCase().contains(STOP);
        boolean runningStatus = !stopRunning && Instance.config.isOn(Config.MAIN);
        return new RunningStatus(isRunning, runningStatus);
    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = instance.gi.webDriver.findElement(By.id("attack_alert"));
            if(attack_alert.getAttribute("class").contains("soon")) {
                return true;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
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
        if(command == null) {
            System.err.println("Skipping resolving null command");
            return;
        }
        if(!session.isLoggedIn()) {
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
//            command.failed++;
//            if (command.failed < 2) {
//                command.retry(2);
//            } else {
//                command.onInterrupt();
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
//            }
        }
        actualProcessedCommand = null;
    }

    private boolean isFleetFreeSlot() {
        return getFleetFreeSlots() > 0;
    }

    public void setFleetStatus(int fleetCount, int fleetMax) {
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
    }

    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
    }

    public synchronized void pushFleet(AbstractCommand command) {
        pushFleet(command, false);
    }

    public synchronized void pushFleet(AbstractCommand command, boolean withPriority) {
        if (withPriority) {
            fleetActionQueue.addFirst(command);
        } else {
            fleetActionQueue.offer(command);
        }
    }

    public synchronized void push(AbstractCommand command) {
        interfaceActionQueue.offer(command);
    }

    public synchronized List<AbstractCommand> peekQueues() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        if (actualProcessedCommand != null) commandsToView.add(actualProcessedCommand);
        commandsToView.addAll(fleetActionQueue);
        commandsToView.addAll(interfaceActionQueue);
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
