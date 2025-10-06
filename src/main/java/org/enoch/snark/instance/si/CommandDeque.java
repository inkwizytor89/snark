package org.enoch.snark.instance.si;

import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.SendCommand;
import org.enoch.snark.instance.service.Navigator;

import java.util.*;

public class CommandDeque {
    private final List<QueueRunType> runTypes = Arrays.asList(QueueRunType.values());

    private AbstractCommand actualProcessedCommand;
    private final Map<QueueRunType, Deque<AbstractCommand>> actionsMap = new HashMap<>();
    public CommandDeque() {
        runTypes.forEach(type -> actionsMap.put(type, new LinkedList<>()));
    }

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
    private synchronized void pushToAction(AbstractCommand command) {
        Deque<AbstractCommand> deque = actionsMap.get(command.getRunType());
        deque.offer(command);
    }

    public synchronized List<AbstractCommand> peek() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        actionsMap.values().forEach(commandsToView::addAll);
        return commandsToView;
    }

    protected synchronized boolean isEmpty() {
        return peek().isEmpty() && actualProcessedCommand == null;
    }

    public synchronized AbstractCommand pool(){
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

    private boolean canPoll(Deque<AbstractCommand> deque) {
        boolean isFleetFreeSlot = Navigator.isFleetFreeSlot();
        return !deque.isEmpty() && (isFleetFreeSlot || !(deque.peekFirst() instanceof SendCommand));
    }

    public void release() {
        actualProcessedCommand = null;
    }

//    private boolean canPoll(List<FleetEntity> toProcess) {
//        boolean atLeast2Slots = Consumer.getInstance().getFleetMax() - Consumer.getInstance().getFleetCount() > 1;
//        return !toProcess.isEmpty() && atLeast2Slots;
//    }
}
