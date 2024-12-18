package org.enoch.snark.instance.commander;

import org.enoch.snark.common.Debug;
import org.enoch.snark.common.RunningProcessor;
import org.enoch.snark.common.RunningState;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.si.module.ConfigMap;
import org.enoch.snark.instance.si.module.update.UpdateThread;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.si.module.ConfigMap.*;

public class Commander extends Thread {

    private static Commander INSTANCE;
    private final CommandDeque commandDeque = new CommandDeque();
    private final RunningProcessor runningProcessor = new RunningProcessor();

    private final GISession session;
    private boolean isRunning = true;

    private int fleetCount = 0;
    private int fleetMax = 1;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

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
            try {
                Instance.updatePropertiesMap();
                isRunning = isRunning && RunningState.isRunning(updateRunningStatus().getActualState());
                if(!isRunning) continue;

                session.reopenServerIfSessionIsOver();

                if(isSomethingAttacking() && Navigator.getInstance().isExpiredAfterMinutes(2)) {
                    UpdateThread.updateState();
                }

                resolve(commandDeque.pool());
                SleepUtil.pause();
            } catch (org.openqa.selenium.TimeoutException e) {
                System.err.println("TimeoutException znowu");
                System.err.println(e);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void waitingToOpenServerTab() {
        while (!session.isRunning()) {
            SleepUtil.pause();
        }
    }

    public RunningProcessor updateRunningStatus() {
        boolean isOn = Instance.getGlobalMainConfigMap().isOn();
        boolean shouldStop = Instance.getGlobalMainConfigMap().getConfig(MODE, "").toLowerCase().contains(STOP);
        return runningProcessor.update(isOn, shouldStop)
                .logChangedStatus(Commander.class.getName());
    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = GI.getInstance().getWebDriver().findElement(By.id("attack_alert"));
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

            Debug.log(ConfigMap.MAIN, command + " start at " + LocalTime.now());
            success = command.execute();
        } catch (ShipDoNotExists e) {
            e.printStackTrace();
            return;
        } catch (Throwable e) {
            e.printStackTrace();
            success = false;
        }

        if(success) {
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
        Debug.log(ConfigMap.MAIN, command + " start at " + LocalTime.now());
    }

    public void setFleetStatus(int fleetCount, int fleetMax) {
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
    }

    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
    }

    public boolean noBlockingHashInQueue(String hash) {
        return hash == null || peekQueues().stream()
                .filter(command -> command.hash() != null)
                .map(AbstractCommand::hash)
                .noneMatch(s -> s.equals(hash));
    }

    private boolean noBlockingHashInDb(String hash, LocalDateTime date) {
        Long count = FleetDAO.getInstance().hashCount(hash, date);
        return count < 1L;
    }

    public boolean noCommands() {
        return peekQueues().isEmpty();
    }

    public boolean notingToPool() {
        return noCommands() && FleetDAO.getInstance().findToProcess().isEmpty();
    }

    public synchronized void push(AbstractCommand command, String action) {
        String hash = command.hash();
        LocalDateTime now = LocalDateTime.now();
        List<FleetEntity> withHash = FleetDAO.getInstance().findWithHash(hash);
        withHash.sort(Comparator.comparing(o -> o.updated));
        if(withHash.isEmpty()) push(command);
        else if(DELAY_TO_FLEET_THERE.equals(action) && now.isAfter(withHash.getLast().visited)) push(command);
        else if(DELAY_TO_FLEET_BACK.equals(action) && now.isAfter(withHash.getLast().back)) push(command);
    }

    public synchronized void push(AbstractCommand command) {
        if(noBlockingHashInQueue(command.hash()))
            commandDeque.push(command);
    }

    public synchronized void push(AbstractCommand command, LocalDateTime from) {
        if(noBlockingHashInQueue(command.hash()) && noBlockingHashInDb(command.hash(), from))
            commandDeque.push(command);
    }

    public synchronized List<AbstractCommand> peekQueues() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        if (actualProcessedCommand != null) commandsToView.add(actualProcessedCommand);
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
