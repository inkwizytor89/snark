package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.PlanetDAO;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import java.util.List;
import java.util.Optional;

public class PlanetDAOImpl extends AbstractDAOImpl<TargetEntity> implements PlanetDAO {

    public PlanetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
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


}
