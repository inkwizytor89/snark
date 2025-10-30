package org.enoch.snark.instance.si.module.consumer;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.*;
import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.action.command.status.ExecutionStatus;
import org.enoch.snark.common.Debug;
import org.enoch.snark.common.RunningProcessor;
import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.instance.service.PlanetService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.GISession;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.si.CommandDeque;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.action.processor.CommandProcessor;
import org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.time.LocalTime;

import static org.enoch.snark.action.command.status.ExecutionIssue.OTHER;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
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

    private Duration restartPause = new Duration("300S");
    private boolean isRunning = true;

    @Override
    protected boolean shouldWaitForDeque() {
        return false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void onStart() {
    }

    @Override
    public void onStep() {

        try {
            startGiIfNeeded();
            startGiSessionIfNeeded();
            registerDequeIfNeeded();
            Core.isSomethingAttacking = isSomethingAttacking();
            resolve(commandDeque.pool());
        } catch (org.openqa.selenium.TimeoutException e) {
            System.err.println("TimeoutException znowu");
            System.err.println(e);
        } catch (WebDriverException e) {
            System.err.println("WebDriver interrupted "+e.getClass().getName()+": "+e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        commandDeque.release();
    }

    private void startGiIfNeeded() {
        if(gi != null) return;
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
        getSources(PlanetService.NONE)
                .forEach(colony -> commandDeque.push(new OpenPageCommand(UrlComponent.FLEETDISPATCH, colony)
                        .sourceHash(this.getClass().getSimpleName())));
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

    private synchronized void resolve(AbstractCommand command) {
        if(command == null) return;
        command.getStatus().setStatus(IN_PROGRESS);
        boolean success;
        try {

            Debug.log(this, command + " start at " + LocalTime.now());
            success = processor.execute(gi, command);
            if(success) {
                command.getStatus().setSuccess();
                if(command.isFollowingAction()) {
                    new WaitingThread(command.getFollowingAction(), commandDeque).start();
                }
            }

//        } catch (ShipDoNotExists e) {
//            e.printStackTrace();
//            return;
        } catch (Throwable e) {
            success = false;
            System.err.println(e.getMessage());
            command.getStatus().setFailed(OTHER);
            e.printStackTrace();
        }

        CommandStatus status = command.getStatus();
        if(!SUCCESS.equals(status.getStatus())) {
            status.failed();
            if (status.getFailed() < 1) {
                status.setStatus(FAILED);
                commandDeque.push(command);
            } else {
                status.setStatus(CRASHED);
                System.err.println("\n\nTOTAL CRASH: " + command + "\n");
            }
        }
        Debug.log(this, command + " start at " + LocalTime.now());
    }

//    public boolean noBlockingHashInQueue(String hash) {
//        return hash == null || peekQueues().stream()
//                .filter(command -> command.hash() != null)
//                .map(AbstractCommand::hash)
//                .noneMatch(s -> s.equals(hash));
//    }

//    private boolean noBlockingHashInDb(String hash, LocalDateTime date) {
//        Long count = FleetDAO.getInstance().hashCount(hash, date);
//        return count < 1L;
//    }

//    public boolean noCommands() {
//        return peekQueues().isEmpty();
//    }
//
//    public boolean notingToPool() {
//        return noCommands() && FleetDAO.getInstance().findToProcess().isEmpty();
//    }

//    public synchronized void push(AbstractCommand command) {
//        if(noBlockingHashInQueue(command.hash()))
//            commandDeque.pushToAction(command);
//    }

//    public synchronized void push(AbstractCommand command, LocalDateTime from) {
//        if(noBlockingHashInQueue(command.hash()) && noBlockingHashInDb(command.hash(), from))
//            commandDeque.push(command);
//    }

//    public synchronized List<AbstractCommand> peekQueues() {
//        List<AbstractCommand> commandsToView = new ArrayList<>();
//        if (actualProcessedCommand != null) commandsToView.add(actualProcessedCommand);
//        commandsToView.addAll(commandDeque.peek());
//        return commandsToView;
//    }

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

    @Override
    public Duration restartDuration() {
        restartPause.update(map.getConfig(ThreadMap.RESTART_DURATION, "300S"));
        return restartPause;
    }
}
