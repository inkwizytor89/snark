package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.model.SystemView;

import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class GalaxyDAOImpl extends AbstractDAOImpl<GalaxyEntity> implements GalaxyDAO {
private static int poolint = 0;
    public GalaxyDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<GalaxyEntity> getEntitylass() {
        return GalaxyEntity.class;
    }

    @Override
    public void update(SystemView systemView) {
        final Optional<GalaxyEntity> galaxyEntity = find(systemView);
        if(galaxyEntity.isPresent()) {
            galaxyEntity.get().setUpdated(new Timestamp(System.currentTimeMillis()));
        } else {
            GalaxyEntity entity = new GalaxyEntity();
            entity.universe = universeEntity;
            entity.setGalaxy(systemView.galaxy);
            entity.setSystem(systemView.system);
            entity.setUpdated(new Timestamp(System.currentTimeMillis()));
            saveOrUpdate(entity);
        }
    }


    @Override
    public Optional<GalaxyEntity> find(SystemView systemView) {
        System.err.println(++poolint);
        final List<GalaxyEntity> result = entityManager.createQuery("" +
                "from GalaxyEntity " +
                "where universe = :universe and " +
                "       galaxy = :galaxy and" +
                "       system = :system", GalaxyEntity.class)
                .setParameter("universe", universeEntity)
                .setParameter("galaxy", systemView.galaxy)
                .setParameter("system", systemView.system)
                .getResultList();
        if(result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }


    @Override
    public List<GalaxyEntity> findLatestGalaxyToView() {
        return entityManager.createQuery(
                "from GalaxyEntity where universe = :universe " +
                        "order by updated asc", GalaxyEntity.class)
                .setParameter("universe", universeEntity)
                .setMaxResults(10)
                .getResultList();
    }
}
