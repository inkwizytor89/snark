package org.enoch.snark.instance;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.gi.GISession;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.gi.command.impl.CommandType;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

public class Commander {

    private static final Logger log = Logger.getLogger(Commander.class.getName() );

    private static final int SLEEP_PAUSE = 1;
    private static Commander INSTANCE;

    private Instance instance;
    private GISession session;
    private boolean isRunning = true;

    private int fleetCount = 0;
    private int fleetMax = 0;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private Queue<AbstractCommand> fleetActionQueue = new LinkedList<>();
    private Queue<AbstractCommand> interfaceActionQueue = new LinkedList<>();
    private AbstractCommand actualProcessedCommand = null;

    public Commander() {
        this.instance = Instance.getInstance();
        this.session = instance.session;
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
            while(true) {
                try {
                    restartIfSessionIsOver();

                    if (instance.isStopped()) {
                        stopCommander();
                        continue;
                    }

                    startCommander();

                    if (!fleetActionQueue.isEmpty() && isFleetFreeSlot()) {
                        actualProcessedCommand = fleetActionQueue.poll();
                        resolve(actualProcessedCommand);
                        actualProcessedCommand = null;
                        fleetCount++;
                        continue;
                    } else if (!interfaceActionQueue.isEmpty()) {
                        actualProcessedCommand = interfaceActionQueue.poll();
                        resolve(actualProcessedCommand);
                        actualProcessedCommand = null;
                        continue;
                    }

                    SleepUtil.secondsToSleep(SLEEP_PAUSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
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
            stopCommander();
            System.err.println("sleep 500 before restart");
            SleepUtil.secondsToSleep(500);
            instance.browserReset();
            startCommander();
        }
    }

    private synchronized void resolve(AbstractCommand command) {
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
            log.info(command.toString());
            if(command.isAfterCommand()) {
                log.info("Next move in "+ command.secondsToDelay+"s is "+ command.getAfterCommand());
                command.doAfter();
            }
        } else {
            command.failed++;
            if (command.failed < 3) {
                command.retry(2);
            } else {
                command.onInterrupt();
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
            }
        }
    }

    private boolean isFleetFreeSlot() {
        if(!session.isLoggedIn()) return false;
        if(getFleetFreeSlots() > 0)   return true;
        return false;
    }

    public void setFleetStatus(int fleetCount, int fleetMax) {
        System.err.println("Fleet status "+fleetCount+"/"+fleetMax);
        this.fleetCount = fleetCount;
        this.fleetMax = fleetMax;
    }

    public void setExpeditionStatus(int expeditionCount, int expeditionMax) {
        this.expeditionCount = expeditionCount;
        this.expeditionMax = expeditionMax;
    }

    public synchronized void push(AbstractCommand command) {
        if (CommandType.FLEET_REQUIERED.equals(command.getType())) {
            fleetActionQueue.offer(command);
        } else if (CommandType.INTERFACE_REQUIERED.equals(command.getType())) {
            interfaceActionQueue.offer(command);
        } else {
            throw new RuntimeException("Invalid type of command");
        }
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
