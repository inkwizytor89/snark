package org.enoch.snark.instance.si.module.consumer;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.LoadColoniesCommand;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.action.command.UpdateResearchCommand;
import org.enoch.snark.common.Debug;
import org.enoch.snark.common.RunningProcessor;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.GISession;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.si.CommandDeque;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.action.processor.CommandProcessor;
import org.enoch.snark.instance.si.module.update.UpdateThread;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;
import static org.enoch.snark.instance.si.module.ThreadMap.*;
import static org.enoch.snark.instance.si.module.consumer.gi.SessionGIR.GF_TOKEN_PRODUCTION;

@RequiredArgsConstructor
public class ConsumerThread extends AbstractThread implements Credentials {

    public static final String threadType = "consumer";

    private final CommandProcessor processor;
    private final CacheEntryRepository cacheEntryRepository;

    private CommandDeque commandDeque;
    private final RunningProcessor runningProcessor = new RunningProcessor();

    private GI gi;
    private GISession session;

    private boolean isRunning = true;

    private int fleetCount = 0;
    private int fleetMax = 1;
    private int expeditionCount = 0;
    private int expeditionMax = 0;

    private AbstractCommand actualProcessedCommand = null;


//    public Consumer(ThreadMap map) {
//        super(map);
//    }

    @Override
    protected boolean shouldWaitForDeque() {
        return false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onStep() {

        try {
            startGiIfNeeded();
            startGiSessionIfNeeded();
            registerDequeIfNeeded();
//        waitingToOpenServerTab();
//        while(true) {
//                isRunning = isRunning && RunningState.isRunning(updateRunningStatus().getActualState());
//                if(!isRunning) continue;



                if(isSomethingAttacking() && Navigator.getInstance().isExpiredAfterMinutes(2)) {
                    UpdateThread.updateState();
                }

                resolve(commandDeque.pool());
            } catch (org.openqa.selenium.TimeoutException e) {
                System.err.println("TimeoutException znowu");
                System.err.println(e);
            } catch (Throwable e) {
                e.printStackTrace();
            }
//        }
    }

    private void startGiIfNeeded() {
        if(gi != null) return;
//        String pathToDriver = map.getConfig(WEBDRIVER_PATH, "C:\\global\\selenium\\chromedriver.exe");
        gi = new GI(this);
        session = gi.getGiSession();
    }

    private void startGiSessionIfNeeded() {
        if(!session.isNeededToRestart()) return;

        String cachedLobbyToken = token();
        String currentLobbyToken = session.reopenServerIfSessionIsOver(this);
        if(!currentLobbyToken.equals(cachedLobbyToken))
            cacheEntryRepository.setValue(GF_TOKEN_PRODUCTION, currentLobbyToken);
    }

    private void registerDequeIfNeeded() {
        if(commandDeque != null) return;
        commandDeque = new CommandDeque();
        commandDeque.push(new LoadColoniesCommand());
        commandDeque.push(new UpdateFleetEventsCommand());
        commandDeque.push(new UpdateResearchCommand());
//        getSources().forEach(colony -> commandDeque.push(
//                new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName())));
        core.register(commandDeque);
    }

//    private void waitingToOpenServerTab() {
//        while (!session.isRunning()) {
//            SleepUtil.pause();
//        }
//    }

//    public RunningProcessor updateRunningStatus() {
//        boolean isOn = Instance.getGlobalMainConfigMap().isOn();
//        boolean shouldStop = Instance.getGlobalMainConfigMap().getConfig(MODE, "").toLowerCase().contains(STOP);
//        return runningProcessor.update(isOn, shouldStop)
//                .logChangedStatus(ConsumerThread.class.getName());
//    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = gi.getWebDriver().findElement(By.id("attack_alert"));
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

            Debug.log(this, command + " start at " + LocalTime.now());
            success = processor.execute(gi, command);

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
        Debug.log(this, command + " start at " + LocalTime.now());
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

    @Override
    public String login() {
        return map.getConfig(ThreadMap.LOGIN);
    }

    @Override
    public String password() {
        return map.getConfig(ThreadMap.PASSWORD);
    }

    @Override
    public String token() {
        return cacheEntryRepository.getValue(GF_TOKEN_PRODUCTION);
    }

    @Override
    public String server() {
        return map.getConfig(ThreadMap.SERVER);
    }

    @Override
    public String hash() {
        return "";
    }
}
