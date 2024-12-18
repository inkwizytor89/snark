package org.enoch.snark.instance.si;

import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.AbstractCommand;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.instance.si.module.consumer.Consumer;

import java.util.*;

public class CommandDeque {
    private final Map<QueueRunType, Deque<AbstractCommand>> actionsMap = new HashMap<>();
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
    public CommandDeque() {
        runTypes.forEach(type -> actionsMap.put(type, new LinkedList<>()));
    }

    public synchronized void push(AbstractCommand command) {
        Deque<AbstractCommand> deque = actionsMap.get(command.getRunType());
        deque.offer(command);
    }

    protected synchronized List<AbstractCommand> peek() {
        List<AbstractCommand> commandsToView = new ArrayList<>();
        actionsMap.values().forEach(commandsToView::addAll);
        return commandsToView;
    }

    protected synchronized boolean isEmpty() {
        return peek().isEmpty();
    }

    protected synchronized AbstractCommand pool(){
        for (QueueRunType type : runTypes) {
            Deque<AbstractCommand> deque = actionsMap.get(type);
            if (canPoll(deque)) {
                return deque.poll();
            }
        }

        List<FleetEntity> toProcess = FleetDAO.getInstance().findToProcess();
        if (canPoll(toProcess)) {
            return new SendFleetCommand(toProcess.get(0));
        }
        return null;
    }

    private boolean canPoll(Deque<AbstractCommand> deque) {
        boolean isFleetFreeSlot = Consumer.getInstance().isFleetFreeSlot();
        return !deque.isEmpty() && (isFleetFreeSlot || !(deque.peekFirst() instanceof SendFleetCommand));
    }

    private boolean canPoll(List<FleetEntity> toProcess) {
        boolean atLeast2Slots = Consumer.getInstance().getFleetMax() - Consumer.getInstance().getFleetCount() > 1;
        return !toProcess.isEmpty() && atLeast2Slots;
    }
}
