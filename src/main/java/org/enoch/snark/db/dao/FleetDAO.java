package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.types.ColonyType;

import javax.persistence.EntityTransaction;
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

    public List<FleetEntity> findToProcess() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FleetEntity " +
                    "where start is null ", FleetEntity.class)
                    .getResultList();
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
            entityManager.getTransaction().begin();
            for(TargetEntity farm : farmTargets) {
                FleetEntity fleet;
                if(Mission.SPY.equals(mission)) {
                    fleet = FleetEntity.createSpyFleet(farm);
                } else if(Mission.ATTACK.equals(mission)) {
                    fleet = FleetEntity.createFarmFleet(farm);
                } else throw new RuntimeException("Unknown mission on farm wave");

                fleet.spaceTarget = ColonyType.PLANET;
                fleet.code = code;
                entityManager.persist(fleet);
                entityManager.flush();
                entityManager.clear();
            }

            entityManager.getTransaction().commit();
        }
    }
}
