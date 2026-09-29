package org.enoch.snark.instance.si.module;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.status.ExecutionStatus;

import java.util.*;

import static org.enoch.snark.action.command.status.ExecutionStatus.*;

public class CommandContainer {

    protected Set<String> keySet = new HashSet<>();
    protected ListMultimap<String, AbstractCommand> incomingMap = ArrayListMultimap.create();
    protected ListMultimap<String, AbstractCommand> inProgressMap = ArrayListMultimap.create();
    protected ListMultimap<String, AbstractCommand> successMap = ArrayListMultimap.create();
    protected ListMultimap<String, AbstractCommand> crashMap = ArrayListMultimap.create();

    public boolean isEmpty() {
        return keySet.isEmpty();
    }

    public boolean contains(String chainKey) {
        return keySet.contains(chainKey);
    }

    public int inProgressCount() {
        return inProgressMap.size();
    }

    public int incomingCount() {
        return incomingMap.size();
    }

    public int size() {
        return keySet.size();
    }

    public boolean anyNotProcessed() {
        return !incomingMap.isEmpty() || !inProgressMap.isEmpty();
    }

    public boolean anyNotProcessed(String chainKey) {
        return incomingMap.containsKey(chainKey) || inProgressMap.containsKey(chainKey);
    }

    private static boolean isExecutedChain(List<AbstractCommand> commandList) {
        return commandList.stream().allMatch(AbstractCommand::executed);
    }

    public void create() {
        keySet = new HashSet<>();
        incomingMap = ArrayListMultimap.create();
        inProgressMap = ArrayListMultimap.create();
        successMap = ArrayListMultimap.create();
        crashMap = ArrayListMultimap.create();
    }

    public void pushCommand(String key, AbstractCommand command) {
        if (command == null) return;
        putCommandChain(key, command);
    }

    private void putCommandChain(String key, AbstractCommand command) {
        if(incomingMap.containsKey(key) || inProgressMap.containsKey(key))
            return;
        removeKey(key);
        keySet.add(key);
        while (command != null) {
            incomingMap.put(key, command);
            command.getStatus().setStatus(WAITING);
            if (command.isFollowingAction()) {
                command = command.getFollowingAction().getCommand();
            } else break;
        }
    }

    public void removeKey(String key) {
        keySet.remove(key);
        incomingMap.removeAll(key);
        inProgressMap.removeAll(key);
        successMap.removeAll(key);
        crashMap.removeAll(key);
    }

    public List<AbstractCommand> pool(Long size) {
        List<AbstractCommand> pooledList = incomingMap.asMap().values().stream()
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().findFirst().get())
                .limit(size)
                .toList();
        return pooledList;
    }

    public void updateMap(ExecutionStatus processingStatus) {
        Map<String, ExecutionStatus> toChange = new HashMap<>();
        ListMultimap<String, AbstractCommand> processingMap = chooseMap(processingStatus);
        processingMap.asMap().entrySet().stream().forEach(entry -> {
            Collection<AbstractCommand> commandChain = entry.getValue();
            boolean allWaiting = commandChain.stream().allMatch(command -> WAITING.equals(command.getStatus().getStatus()));
            if(allWaiting) {
                toChange.put(entry.getKey(), WAITING);
                return;
            }
            boolean allSuccess = commandChain.stream().allMatch(command -> SUCCESS.equals(command.getStatus().getStatus()));
            if(allSuccess) {
                toChange.put(entry.getKey(), SUCCESS);
                return;
            }
            boolean anyCrashed = commandChain.stream().allMatch(command -> CRASHED.equals(command.getStatus().getStatus()));
            if(anyCrashed) {
                toChange.put(entry.getKey(), CRASHED);
                return;
            }
            // problem is when will stuck
            toChange.put(entry.getKey(), IN_PROGRESS);
        });

        toChange.entrySet().stream().forEach(entry -> {
            if(processingStatus.equals(entry.getValue())) return;

            List<AbstractCommand> commandChain = new ArrayList<>(processingMap.get(entry.getKey()));
            ListMultimap<String, AbstractCommand> targetMap = chooseMap(entry.getValue());
            targetMap.putAll(entry.getKey(), commandChain);
            processingMap.removeAll(entry.getKey());
        });
    }

    private ListMultimap<String, AbstractCommand> chooseMap(ExecutionStatus processingStatus) {
        if(CRASHED.equals(processingStatus)) return crashMap;
        else if(SUCCESS.equals(processingStatus)) return successMap;
        else if(IN_PROGRESS.equals(processingStatus)) return inProgressMap;
        else if(WAITING.equals(processingStatus)) return incomingMap;
        else throw new IllegalStateException("Can not choose map for ExecutionStatus "+processingStatus);
    }

    public List<AbstractCommand> peek() {
        ArrayList<AbstractCommand> result = new ArrayList<>();
        result.addAll(incomingMap.values());
        result.addAll(inProgressMap.values());
        result.addAll(successMap.values());
        result.addAll(crashMap.values());
        return result;
    }

    @Override
    public String toString() {
        return "all " + size() + "(incomingMap:" + incomingMap.size() + " inProgressMap:" + inProgressMap.size() + " successMap:" + successMap.size() + " crashMap:" + crashMap.size() +")";
    }
}
