package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import java.util.List;
import java.util.Optional;

public class TargetDAOImpl extends AbstractDAOImpl<TargetEntity> implements TargetDAO {

    public TargetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<TargetEntity> getEntitylass() {
        return TargetEntity.class;
    }

    @Override
    public Optional<TargetEntity> find(Integer galaxy, Integer system, Integer position) {
        final List<TargetEntity> result = entityManager.createQuery("" +
                "from TargetEntity " +
                "where universe = :universe and " +
                "       galaxy = :galaxy and" +
                "       system = :system and " +
                "       position = :position", TargetEntity.class)
                .setParameter("universe", universeEntity)
                .setParameter("galaxy", galaxy)
                .setParameter("system", system)
                .setParameter("position", position)
                .getResultList();
        if(result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public List<TargetEntity> findFarms(Integer limit) {
        final List<TargetEntity> result = entityManager.createQuery("" +
                "from TargetEntity " +
                "where universe = :universe and " +
                "       fleet_sum = 0" +
                "       defense_sum = 0" +
                "order by power desc " +
                "limit :limit", TargetEntity.class)
                .setParameter("universe", universeEntity)
                .setParameter("limit", limit)
                .getResultList();
        return result;
    }

    @Override
    public List<TargetEntity> findTopFarms(int limit) {
        final List<TargetEntity> result = entityManager.createQuery("" +
                "from TargetEntity " +
                "where universe = :universe and " +
                "       fleet_sum = 0" +
                "       defense_sum = 0" +
                "order by resources desc " +
                "limit :limit", TargetEntity.class)
                .setParameter("universe", universeEntity)
                .setParameter("limit", limit)
                .getResultList();
        return result;
    }

    @Override
    public Optional<TargetEntity> findNotScaned() {
        return Optional.ofNullable(entityManager.createQuery("" +
                "from TargetEntity " +
                "where universe = :universe and " +
                "       fleet_sum is null and " +
                "       defense_sum is null and " +
                "       type = :type " +
                "order by resources desc " +
                "limit :limit", TargetEntity.class)
                .setParameter("universe", universeEntity)
                .setParameter("type", TargetEntity.IN_ACTIVE)
                .setParameter("limit", 1)
                .getSingleResult());
    }

}
