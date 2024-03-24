package org.enoch.snark.instance.commander;

import org.enoch.snark.common.RunningStatus;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.si.module.update.UpdateThread;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.enoch.snark.instance.si.module.ConfigMap.MODE;
import static org.enoch.snark.instance.si.module.ConfigMap.STOP;

public class Commander extends Thread {

    private static Commander INSTANCE;
    private CommandDeque commandDeque = new CommandDeque();

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

                session.reopenServerIfSessionIsOver();

//                if (instance.isStopped()) {
//                    stopCommander();
//                    continue;
//                }

//                startCommander();


                if(isSomethingAttacking() && Navigator.getInstance().isExpiredAfterMinutes(2)) {
                    UpdateThread.updateState();
                }

//                // check if fleet slot is free because is something fleet to send
//                if(!isFleetFreeSlot() && !fleetActionQueue.isEmpty()) {
//                    resolve(new OpenPageCommand(FLEETDISPATCH));
//                }
//                // method for put top in fleet queue
//                if(isFleetFreeSlot()) {
//                    if (!fleetActionQueue.isEmpty()) {
//                        resolve(Objects.requireNonNull(fleetActionQueue.poll()));
//                        SleepUtil.sleep();
//                        continue;
//                    }
//                }
//                if (!interfaceActionQueue.isEmpty()) {
//                        resolve(interfaceActionQueue.poll());
//                        continue;
//                }
//                if(isFleetFreeSlot()) {
//                    List<FleetEntity> toProcess = FleetDAO.getInstance().findToProcess();
//                    if (!toProcess.isEmpty()) {
//                        resolve(new SendFleetCommand(toProcess.get(0)));
//                        SleepUtil.sleep();
//                        fleetCount++;
//                        continue;
//                    }
//                }
                resolve(commandDeque.pool());
                SleepUtil.sleep();
            } catch (org.openqa.selenium.TimeoutException e) {
                System.err.println("TimeoutException znowu");
                System.err.println(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void waitingToOpenServerTab() {
        while (!session.isRunning()) {
            SleepUtil.pause();
        }
    }

    public RunningStatus createRunningStatus() {
        boolean stopRunning = Instance.getMainConfigMap().getConfig(MODE, "").toLowerCase().contains(STOP);
        boolean runningStatus = !stopRunning && Instance.getMainConfigMap().isOn();
        return new RunningStatus(isRunning, runningStatus);
    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = GI.getInstance().webDriver.findElement(By.id("attack_alert"));
            if(attack_alert.getAttribute("class").contains("soon")) {
                return true;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }

    public void startCommander() {
        System.err.println("Commander is startedped");
        this.isRunning = true;
    }

    public void stopCommander() {
        System.err.println("Commander is stopped");
        this.isRunning = false;
    }

    private synchronized void resolve(AbstractCommand command) {
        actualProcessedCommand = command;
        boolean success;
        if(command == null) {
            return;
        }
        try {
            success = command.execute();
        } catch (ShipDoNotExists e) {
            e.printStackTrace();
            return;
        } catch (TimeoutException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            success = false;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            success = false;
        }

        if(success) {
            String commandMessage = command.toString();
//            log.info(command.toString());
            if(command.isFollowingAction()) {
                command.doFallowing();
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

    public void setFleetStatus(int fleetCount, int fleetMax) {
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
    }

    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
    }

    public boolean noBlockingHash(String hash) {
        return hash == null || peekQueues().stream()
                .filter(command -> command.hash() != null)
                .map(AbstractCommand::hash)
                .noneMatch(s -> s.equals(hash));
    }

    public boolean noCommands() {
        return peekQueues().isEmpty();
    }

    public boolean notingToPool() {
        return noCommands() && FleetDAO.getInstance().findToProcess().isEmpty();
    }

    public synchronized void push(AbstractCommand command) {
//        if(MAJOR.equals(command.getRunType())) fleetActionQueue.addFirst(command);
//        else if (NORMAL.equals(command.getRunType())) fleetActionQueue.offer(command);
//        else interfaceActionQueue.offer(command);
        if(noBlockingHash(command.hash()))
            commandDeque.push(command);
    }

    public synchronized List<AbstractCommand> peekQueues() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        if (actualProcessedCommand != null) commandsToView.add(actualProcessedCommand);
//        commandsToView.addAll(fleetActionQueue);
//        commandsToView.addAll(interfaceActionQueue);
        commandsToView.addAll(commandDeque.peek());
        return commandsToView;
    }

    public boolean isFleetFreeSlot() {
        return getFleetFreeSlots() > 0;
    }

    public int getFleetFreeSlots() {
        return fleetMax - fleetCount;
    }

    public int getFleetCount() {
        return fleetCount;
    }

    public int getFleetMax() {
        return fleetMax;
    }

    public int getExpeditionFreeSlots() {
        return expeditionMax - expeditionCount;
    }

    public int getExpeditionCount() {
        return expeditionCount;
    }

    public int getExpeditionMax() {
        return expeditionMax;
    }
}
