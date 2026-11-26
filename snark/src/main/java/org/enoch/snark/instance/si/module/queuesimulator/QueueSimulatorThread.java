package org.enoch.snark.instance.si.module.queuesimulator;

import lombok.extern.slf4j.Slf4j;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.ArrayList;
import java.util.List;
import org.enoch.snark.instance.si.CommandDeque;

import static org.enoch.snark.action.command.status.ExecutionStatus.IN_PROGRESS;

@Slf4j
public class QueueSimulatorThread extends AbstractThread {

    public static final String threadType = "queue-simulator";

    private QueueSimulatorDefinition config;
    private CommandDeque commandDeque;
    private final List<AbstractCommand> processedCommands = new ArrayList<>();

    @Override
    protected boolean shouldWaitForDeque() {
        return false;
    }

    @Override
    protected void onStart() {
        config = new QueueSimulatorDefinition();
        config.update(threadEntity.configs);
        registerDequeIfNeeded();
    }

    @Override
    protected void onStep() {
        processNextCommand();
    }

    public synchronized void processNextCommand() {
        registerDequeIfNeeded();
        AbstractCommand command = commandDeque.pool();
        if (command == null) {
            return;
        }

        try {
            command.getStatus().setStatus(IN_PROGRESS);
            command.getStatus().setSuccess();
            command.getStatus().setIssue(ExecutionIssue.NO_ISSUE);
            processedCommands.add(command);

            if (command.isFollowingAction()) {
                commandDeque.push(command.getFollowingAction().getCommand());
            }
        } finally {
            commandDeque.release();
        }
    }

    private void registerDequeIfNeeded() {
        if (commandDeque != null) {
            return;
        }
        commandDeque = new CommandDeque();
        core.register(commandDeque);
    }

    public synchronized List<AbstractCommand> getProcessedCommands() {
        return List.copyOf(processedCommands);
    }

    public synchronized CommandDeque getCommandDeque() {
        registerDequeIfNeeded();
        return commandDeque;
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        if (config == null) {
            config = new QueueSimulatorDefinition();
            config.update(threadEntity.configs);
        }
        return config.threadConfiguration(threadEntity.name, threadEntity.module.name);
    }
}
