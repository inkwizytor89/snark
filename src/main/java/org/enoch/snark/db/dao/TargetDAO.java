package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.TargetEntity;

import java.util.List;
import java.util.Optional;

public class TargetDAO extends AbstractDAO<TargetEntity> {

    private static TargetDAO INSTANCE;

    private TargetDAO() {
        super();
    }

    public static TargetDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TargetDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<TargetEntity> getEntitylass() {
        return TargetEntity.class;
    }

    public Optional<TargetEntity> find(Integer galaxy, Integer system, Integer position) {
        synchronized (JPAUtility.dbSynchro) {
            final List<TargetEntity> result = entityManager.createQuery("" +
                    "from TargetEntity " +
                    "where galaxy = :galaxy and " +
                    "       system = :system and " +
                    "       position = :position ", TargetEntity.class)
                    .setParameter("galaxy", galaxy)
                    .setParameter("system", system)
                    .setParameter("position", position)
                    .getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result.get(0));
        }
    }

    public List<TargetEntity> find(Integer galaxy, Integer system) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from TargetEntity " +
                    "where galaxy = :galaxy and " +
                    "       system = :system ", TargetEntity.class)
                    .setParameter("galaxy", galaxy)
                    .setParameter("system", system)
                    .getResultList();
        }
    }

    public List<TargetEntity> findFarms(Integer limit) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from TargetEntity " +
                    "where fleet_sum = 0 and " +
                    "       defense_sum = 0 and " +
                    "       type = 'IN_ACTIVE' " +
                    "order by power desc ", TargetEntity.class)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public List<TargetEntity> findTopFarms(int limit) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from TargetEntity " +
                    "where fleet_sum = 0 and " +
                    "       defense_sum = 0 and " +
                    "       resources is not null and " +
                    "       resources > 0 " +
                    "order by resources desc ", TargetEntity.class)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public Optional<TargetEntity> findNotScaned() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from TargetEntity " +
                    "where fleet_sum is null and " +
                    "       defense_sum is null and " +
                    "       type = :type " +
                    "order by resources desc ", TargetEntity.class)
                    .setParameter("type", TargetEntity.IN_ACTIVE)
                    .setMaxResults(1)
                    .getResultList().stream().findFirst();
        }
    }

}
