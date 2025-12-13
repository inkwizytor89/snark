package org.enoch.snark.instance.si.module;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.enoch.snark.action.command.AbstractCommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.enoch.snark.action.command.status.ExecutionStatus.WAITING;

public class CommandContainer {

    protected ListMultimap<String, AbstractCommand> commandsMap = ArrayListMultimap.create();

    private Queue<String> incoming = new LinkedList<>();
    private List<String> inProgress = new ArrayList<>();
    private List<String> executed = new ArrayList<>();

    public boolean isEmpty() {
        return commandsMap.isEmpty();
    }

    public boolean contains(String chainKey) {
        return commandsMap.containsKey(chainKey);
    }

    public int inProgressCount() {
        return inProgress.size();
    }

    public int incomingCount() {
        return incoming.size();
    }

    public int size() {
        return commandsMap.keySet().size();
    }

    public boolean anyNotProcessed() {
        return !incoming.isEmpty() || !inProgress.isEmpty();
    }

    public boolean anyNotProcessed(String chainKey) {
        return !isExecutedChain(commandsMap.get(chainKey));
    }

    private static boolean isExecutedChain(List<AbstractCommand> commandList) {
        return commandList.stream().allMatch(AbstractCommand::executed);
    }

    public void create() {
        commandsMap = ArrayListMultimap.create();
        incoming = new LinkedList<>();
        inProgress = new ArrayList<>();
        executed = new ArrayList<>();
    }

    public void pushCommand(String key, AbstractCommand command) {
        if (command == null) return;
        command.getStatus().setStatus(WAITING);
        putCommandChain(key, command);
    }

    private void putCommandChain(String key, AbstractCommand command) {
        commandsMap.removeAll(key);
        incoming.add(key);
        while (command != null) {
            commandsMap.put(key, command);
            if (command.isFollowingAction()) {
                command = command.getFollowingAction().getCommand();
            } else break;
        }
    }

    public AbstractCommand pool() {
        String keyToPoll = incoming.poll();
        if(keyToPoll == null) return null;
        AbstractCommand polled = commandsMap.get(keyToPoll).getFirst();
        if (polled != null) inProgress.add(keyToPoll);
        return polled;
    }

    public void recalculate() {
        new ArrayList<>(inProgress).stream().
                filter(key -> isExecutedChain(commandsMap.get(key)))
                .forEach(key -> {
                    inProgress.remove(key);
                    executed.add(key);
                });
}

@Override
public String toString() {
    return "all " + size() + "(incoming:" + incoming.size() + " inProgress:" + inProgress.size() + " executed:" + executed.size() + ")";
}
}
