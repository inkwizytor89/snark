package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.types.ColonyType;

import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public class FleetDAO extends AbstractDAO<FleetEntity> {

    private static FleetDAO INSTANCE;

    private FleetDAO() {
        super();
    }

    public static FleetDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FleetDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<FleetEntity> getEntityClass() {
        return FleetEntity.class;
    }

    public Long generateNewCode() {
        synchronized (JPAUtility.dbSynchro) {
            Long singleResult = entityManager.createQuery("" +
                    "select max(e.code) from FleetEntity e", Long.class)
                    .getSingleResult();
            if(singleResult == null) {
                singleResult = 1L;
            }
            return singleResult + 1;
        }
    }

    public List<FleetEntity> findWithCode(Long code) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where code = :code", FleetEntity.class)
                    .setParameter("code", code)
                    .getResultList();
        }
    }

    public List<FleetEntity> findWithHash(String hash) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery(
                    "from FleetEntity " +
                            "where hash = :hash", FleetEntity.class)
                    .setParameter("hash", hash)
                    .getResultList();
        }
    }

    public List<FleetEntity> findToProcess() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where start is null ", FleetEntity.class)
                    .getResultList();
//            mar 25, 2024 10:36:35 AM org.hibernate.AssertionFailure <init>
//            ERROR: HHH000099: an assertion failure occurred (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session): org.hibernate.AssertionFailure: null id in org.enoch.snark.db.entity.FleetEntity entry (don't flush the Session after an exception occurs)
//            org.hibernate.AssertionFailure: null id in org.enoch.snark.db.entity.FleetEntity entry (don't flush the Session after an exception occurs)
//            at org.hibernate.event.internal.DefaultFlushEntityEventListener.checkId(DefaultFlushEntityEventListener.java:71)
//            at org.hibernate.event.internal.DefaultFlushEntityEventListener.getValues(DefaultFlushEntityEventListener.java:186)
//            at org.hibernate.event.internal.DefaultFlushEntityEventListener.onFlushEntity(DefaultFlushEntityEventListener.java:146)
//            at org.hibernate.event.internal.AbstractFlushingEventListener.flushEntities(AbstractFlushingEventListener.java:235)
//            at org.hibernate.event.internal.AbstractFlushingEventListener.flushEverythingToExecutions(AbstractFlushingEventListener.java:94)
//            at org.hibernate.event.internal.DefaultAutoFlushEventListener.onAutoFlush(DefaultAutoFlushEventListener.java:44)
//            at org.hibernate.internal.SessionImpl.autoFlushIfRequired(SessionImpl.java:1445)
//            at org.hibernate.internal.SessionImpl.list(SessionImpl.java:1531)
//            at org.hibernate.query.internal.AbstractProducedQuery.doList(AbstractProducedQuery.java:1561)
//            at org.hibernate.query.internal.AbstractProducedQuery.list(AbstractProducedQuery.java:1529)
//            at org.hibernate.query.Query.getResultList(Query.java:168)
//            at org.enoch.snark.db.dao.FleetDAO.findToProcess(FleetDAO.java:61)
//            at org.enoch.snark.instance.commander.CommandDeque.pool(CommandDeque.java:53)
//            at org.enoch.snark.instance.commander.Commander.run(Commander.java:69)
        }
    }

    public List<FleetEntity> findLastSend(LocalDateTime from) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where visited > :from or start = null", FleetEntity.class)
                    .setParameter("from", from)
                    .getResultList();
        }
    }

    public List<FleetEntity> findWithTag(LocalDateTime from, String tag) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where visited > :from or start = null and " +
                    " false", FleetEntity.class)
                    .setParameter("from", from)
                    .setParameter("tag", tag)
                    .getResultList();
        }
    }

    public Long hashCount(String hash, LocalDateTime from) {
        synchronized (JPAUtility.dbSynchro) {
            Long singleResult = entityManager.createQuery("" +
                    "select count(e.hash) from FleetEntity e " +
                    "where updated > :from and hash = :hash  ", Long.class)
                    .setParameter("from", from)
                    .setParameter("hash", hash)
                    .getSingleResult();
            if(singleResult == null) return 0L;
            return singleResult;
        }
    }

    public void clean(LocalDateTime from) {
        synchronized (JPAUtility.dbSynchro) {

            if(JPAUtility.syncMethod != null) System.err.println("Error: synchronization collision with "+JPAUtility.syncMethod);
            JPAUtility.syncMethod = "org.enoch.snark.db.dao.FleetDAO.clean by date";

            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createQuery("" +
                    "delete from FleetEntity " +
                    "where updated < :from ")
                    .setParameter("from", from)
                    .executeUpdate();

            JPAUtility.syncMethod = null;
            entityManager.flush();
            transaction.commit();
        }
    }

    public void clean(ColonyEntity colony) {
        synchronized (JPAUtility.dbSynchro) {

            if(JPAUtility.syncMethod != null) System.err.println("Error: synchronization collision with "+JPAUtility.syncMethod);
            JPAUtility.syncMethod = "org.enoch.snark.db.dao.FleetDAO.clean by ColonyEntity";

            final EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createQuery("" +
                    "delete from FleetEntity " +
                    "where source = :colony ")
                    .setParameter("colony", colony)
                    .executeUpdate();

            JPAUtility.syncMethod = null;
            entityManager.flush();
            transaction.commit();
        }
    }

    public void createNewWave(Mission mission, List<TargetEntity> farmTargets, Long code) {
        synchronized (JPAUtility.dbSynchro) {

            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                for (TargetEntity farm : farmTargets) {
                    FleetEntity fleet;
                    if (Mission.SPY.equals(mission)) {
                        fleet = FleetEntity.createSpyFleet(farm);
                    } else if (Mission.ATTACK.equals(mission)) {
                        fleet = FleetEntity.createFarmFleet(farm);
                    } else throw new RuntimeException("Unknown mission on farm wave");

                    fleet.spaceTarget = ColonyType.PLANET;
                    fleet.code = code;
                    entityManager.persist(fleet);
                    entityManager.flush();
                    entityManager.clear();
                }

                transaction.commit();
            } catch (Exception e) {
                System.err.println("transaction error "+e.getMessage());
                e.printStackTrace();
            } finally {
                if(transaction.isActive()){
                    transaction.rollback();
                }
            }
        }
    }
}
