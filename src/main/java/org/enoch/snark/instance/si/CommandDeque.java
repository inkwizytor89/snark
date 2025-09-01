package org.enoch.snark.instance.si;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.instance.service.Navigator;

import java.time.LocalDateTime;
import java.util.*;

public class CommandDeque {
    private final List<QueueRunType> runTypes = Arrays.asList(QueueRunType.values());
    // todo: check exception
//    ERROR: HHH000099: an assertion failure occurred (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session): org.hibernate.AssertionFailure: null id in org.enoch.snark.db.entity.FleetEntity entry (don't flush the Session after an exception occurs)
//            org.hibernate.AssertionFailure: null id in org.enoch.snark.db.entity.FleetEntity entry (don't flush the Session after an exception occurs)
//            at org.hibernate.event.internal.DefaultFlushEntityEventListener.checkId(DefaultFlushEntityEventListener.java:71)
//    at org.hibernate.event.internal.DefaultFlushEntityEventListener.getValues(DefaultFlushEntityEventListener.java:186)
//    at org.hibernate.event.internal.DefaultFlushEntityEventListener.onFlushEntity(DefaultFlushEntityEventListener.java:146)
//    at org.hibernate.event.internal.AbstractFlushingEventListener.flushEntities(AbstractFlushingEventListener.java:235)
//    at org.hibernate.event.internal.AbstractFlushingEventListener.flushEverythingToExecutions(AbstractFlushingEventListener.java:94)
//    at org.hibernate.event.internal.DefaultAutoFlushEventListener.onAutoFlush(DefaultAutoFlushEventListener.java:44)
//    at org.hibernate.internal.SessionImpl.autoFlushIfRequired(SessionImpl.java:1445)
//    at org.hibernate.internal.SessionImpl.list(SessionImpl.java:1531)
//    at org.hibernate.query.internal.AbstractProducedQuery.doList(AbstractProducedQuery.java:1561)
//    at org.hibernate.query.internal.AbstractProducedQuery.list(AbstractProducedQuery.java:1529)
//    at org.hibernate.query.Query.getResultList(Query.java:168)
//    at org.enoch.snark.db.dao.FleetDAO.findToProcess(FleetDAO.java:61)
//    at org.enoch.snark.instance.si.CommandDeque.pool(CommandDeque.java:39)
//    at org.enoch.snark.instance.si.module.consumer.Commander.run(Commander.java:105)

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

    private boolean noBlockingHashInDb(String hash, LocalDateTime date) {
        Long count = FleetDAO.getInstance().hashCount(hash, date);
        return count < 1L;
    }

    public synchronized void push(AbstractCommand command) {
        if(command.isFollowingAction())
            throw new NotImplementedException("Implementation for send fleet and action is not implemented");

        if(command instanceof SendFleetPromiseCommand && command.from != null ) {
            if (!noBlockingHashInDb(command.hash(), command.from)) return;
        }

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
        return peek().isEmpty();
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

//        List<FleetEntity> toProcess = FleetDAO.getInstance().findToProcess();
//        if (canPoll(toProcess)) {
//            return new SendFleetCommand(toProcess.get(0));
//        }
        return null;
    }

    private boolean canPoll(Deque<AbstractCommand> deque) {
        boolean isFleetFreeSlot = Navigator.isFleetFreeSlot();
        return !deque.isEmpty() && (isFleetFreeSlot || !(deque.peekFirst() instanceof SendFleetPromiseCommand));
    }

    public void release() {
        actualProcessedCommand = null;
    }

//    private boolean canPoll(List<FleetEntity> toProcess) {
//        boolean atLeast2Slots = Consumer.getInstance().getFleetMax() - Consumer.getInstance().getFleetCount() > 1;
//        return !toProcess.isEmpty() && atLeast2Slots;
//    }
}
