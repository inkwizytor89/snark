package org.enoch.snark.instance.si.module.consumer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.*;

import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.common.Debug;
import org.enoch.snark.common.RunningProcessor;
import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.instance.model.exception.FleetIsCurrentlyInCombatException;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.WorkerLifecycle;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.enoch.snark.instance.si.module.consumer.gi.GISession;
import org.enoch.snark.instance.si.module.consumer.gi.BrowserWindowUnavailableException;
import org.enoch.snark.instance.si.CommandDeque;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.action.processor.CommandProcessor;
import org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import static org.enoch.snark.action.command.status.ExecutionIssue.*;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
import static org.enoch.snark.instance.si.module.consumer.gi.SessionGIR.GF_TOKEN_PRODUCTION;

@RequiredArgsConstructor
public class ConsumerThread extends AbstractThread implements Credentials {

    private ConsumerDefinition config;

    private final CommandProcessor processor;
    private final CacheEntryRepository cacheEntryRepository;
    private final WorkerLifecycle workerLifecycle;
    private final ConfigRepository configRepository;

    private CommandDeque commandDeque;
    private final RunningProcessor runningProcessor = new RunningProcessor();

    private Wd wd;
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
        config = new ConsumerDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
    }

    @Override
    public void onStep() {
        try {
            startGiSessionIfNeeded();
            registerDequeIfNeeded();
            Core.isSomethingAttacking = isSomethingAttacking();
            AbstractCommand pool = commandDeque.pool();

            resolve(pool);
        } catch (org.openqa.selenium.TimeoutException e) {
            System.err.println("TimeoutException znowu");
            System.err.println(e);
            terminateIfBrowserWindowWasLost(e);
        } catch (WebDriverException e) {
            System.err.println("WebDriver interrupted "+e.getClass().getName()+": "+e.getMessage());
            System.err.println(e);
            terminateIfBrowserWindowWasLost(e);
        } catch (Throwable e) {
            e.printStackTrace();
            terminateIfBrowserWindowWasLost(e);
        } finally {
            if (commandDeque != null) {
                commandDeque.release();
            }
        }
    }

    private void startGiSessionIfNeeded() {
        if(session == null) {
            System.err.println("startGiSessionIfNeeded session == null");
            session = new GISession(config.getWebdriverPath(), workerLifecycle);
            wd = session.getWd();
        }
        wd.setUrl(config.getUrl());
        wd.setHash(config.getHash());
        wd.setServerName(config.getServer());

        if(!session.isNeededToRestart()) return;

        String cachedLobbyToken = token();
        String currentLobbyToken = session.openNewServerSession(this);
        if(!currentLobbyToken.equals(cachedLobbyToken))
            cacheEntryRepository.setValue(GF_TOKEN_PRODUCTION, currentLobbyToken);
    }

    private void terminateIfBrowserWindowWasLost(Throwable failure) {
        if (failure instanceof BrowserWindowUnavailableException
                || (wd != null && !wd.isDriverAlive())
                || (session != null && !session.isActiveServerWindowAvailable())) {
            workerLifecycle.terminate("ConsumerThread lost its ChromeDriver browser window");
        }
    }

    @PreDestroy
    public void closeBrowser() {
        System.out.println("[SHUTDOWN][CONSUMER] Closing browser; driver initialized=" + (wd != null));
        if (wd == null) {
            return;
        }
        try {
            wd.closeWebDriver();
        } catch (RuntimeException e) {
            System.err.println("[SHUTDOWN][CONSUMER] Browser close failed: " + e.getMessage());
            e.printStackTrace(System.err);
            throw e;
        }
        System.out.println("[SHUTDOWN][CONSUMER] Browser close completed");
    }

    private void registerDequeIfNeeded() {
        if(commandDeque != null) return;
        commandDeque = new CommandDeque();
        commandDeque.push(new LoadColoniesCommand());
//        commandDeque.push(new ReadMessageCommand());
        commandDeque.push(new UpdateFleetEventsCommand());
        commandDeque.push(new UpdateResearchCommand());

        if(!colonyRepository.findAll().isEmpty()) getSources(CoordinateExpressionService.NONE)
                .forEach(colony -> commandDeque.push(new OpenPageCommand(UrlComponent.FLEETDISPATCH, colony)
                        .sourceHash(this.getClass().getSimpleName())));
        core.register(commandDeque);
    }

    private boolean isSomethingAttacking() {
        try {
            WebElement attack_alert = wd.getWebDriver().findElement(By.id("attack_alert"));
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
        ExecutionIssue executionIssue = OTHER;
        try {
            // The consumer thread only pulls from the queue and delegates execution to the processor layer.
            executionIssue = processor.execute(wd, command);
            command.getStatus().setIssue(executionIssue);
            if(NO_ISSUE.equals(executionIssue)) {
                command.getStatus().setSuccess();
                if(command.isFollowingAction()) {
                    new WaitingThread(command.getFollowingAction(), commandDeque).start();
                }
            } if (FLEET_IN_COMBAT.equals(executionIssue)) {

            }

        } catch (FleetIsCurrentlyInCombatException e) {
            command.getStatus().setFailed(FLEET_IN_COMBAT);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            command.getStatus().setFailed(executionIssue);
            e.printStackTrace();
        }

        CommandStatus status = command.getStatus();
        if(!SUCCESS.equals(status.getStatus())) {
            status.failed();
            if (status.getFailed() < 2 || (RETRY.equals(status.getIssue()) && status.getFailed() < 4)) commandDeque.pushFailed(command);
            else status.setStatus(CRASHED);
        }
        Debug.log(this, command.getDebugId()+" "+command.getStatus()+"\t"+command.hash()+" <- "+commandDeque.toString());
    }

    @Override
    public String login() {
        return config.getLogin();
    }

    @Override
    public String password() {
        return config.getPassword();
    }

    @Override
    public String token() {
        return cacheEntryRepository.getValue(GF_TOKEN_PRODUCTION);
    }

    @Override
    public String server() {
        return config.getServer();
    }

    @Override
    public String hash() {
        return config.getHash();
    }

    @Override
    public Duration restartDuration() {
        restartPause.update(config.getRestartDuration());
        return restartPause;
    }
}
