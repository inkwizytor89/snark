package org.enoch.snark.instance.si;

import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.instance.service.Navigator;

import java.time.LocalDateTime;
import java.util.*;

import static org.enoch.snark.action.command.status.ExecutionStatus.NEW;

public class CommandDeque {
    private final List<QueueRunType> runTypes = Arrays.asList(QueueRunType.values());

    private AbstractCommand actualProcessedCommand;
    private final Map<QueueRunType, Deque<AbstractCommand>> actionsMap = new HashMap<>();
    public CommandDeque() {
        runTypes.forEach(type -> actionsMap.put(type, new LinkedList<>()));
    }
    public Map<LocalDateTime, AbstractCommand> waiting = new HashMap<>();

    private boolean noBlockingHashInQueue(String hash) {
        if(hash == null) return true;
        List<AbstractCommand> commandsToCheck = peek();
        if(actualProcessedCommand != null ) commandsToCheck.add(actualProcessedCommand);

        return commandsToCheck.stream()
                .map(AbstractCommand::hash)
                .filter(Objects::nonNull)
                .noneMatch(s -> s.equals(hash));
    }

    public synchronized void push(AbstractCommand command) {
        if(!noBlockingHashInQueue(command.hash())) return;
        pushToAction(command);
    }

    public synchronized void pushFailed(AbstractCommand command) {
        long secondsToDelay = (command.getStatus().getFailed() + 3) * 10L;
        waiting.put(LocalDateTime.now().plusSeconds(secondsToDelay), command);
    }

    private synchronized void pushToAction(AbstractCommand command) {
        Deque<AbstractCommand> deque = actionsMap.get(command.getRunType());
        deque.offer(command);
        command.getStatus().setStatus(NEW);
    }

    public synchronized List<AbstractCommand> peek() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        actionsMap.values().forEach(commandsToView::addAll);
        commandsToView.addAll(waiting.values());
        return commandsToView;
    }

    protected synchronized boolean isEmpty() {
        return peek().isEmpty() && actualProcessedCommand == null;
    }

    public synchronized AbstractCommand pool(){
        releaseWaitingCommands();
        for (QueueRunType type : runTypes) {
            Deque<AbstractCommand> deque = actionsMap.get(type);
            if (canPoll(deque)) {
                AbstractCommand polled = deque.poll();
                actualProcessedCommand = polled;
                return polled;
            }
        }
        return null;
    }

    private void releaseWaitingCommands() {
        Set<Map.Entry<LocalDateTime, AbstractCommand>> entries = new HashSet<>(waiting.entrySet());
        entries.stream()
                .filter(entry -> LocalDateTime.now().isAfter(entry.getKey()))
                .forEach(entry -> {
                    waiting.remove(entry.getKey());
                    pushToAction(entry.getValue());
                });
    }

    private boolean canPoll(Deque<AbstractCommand> deque) {
        boolean isFleetFreeSlot = Navigator.isFleetFreeSlot();
        return !deque.isEmpty() && (isFleetFreeSlot || !(deque.peekFirst() instanceof SendCommand));
    }

    public void release() {
        actualProcessedCommand = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        actionsMap.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(key).append(": [");
                sb.append(String.join(", ", value.stream().map(AbstractCommand::hash).toList()));
                sb.append("]");
            }
        });
        return sb.toString();
    }
}
