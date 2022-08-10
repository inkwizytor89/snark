package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.model.SystemView;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;

public class GalaxyDAOImpl extends AbstractDAOImpl<GalaxyEntity> implements GalaxyDAO {

    public GalaxyDAOImpl(UniverseEntity universeEntity, EntityManager entityManager) {
        super(universeEntity, entityManager);
    }

    @Override
    protected Class<GalaxyEntity> getEntitylass() {
        return GalaxyEntity.class;
    }

    @Override
    public void update(SystemView systemView) {
        final Optional<GalaxyEntity> galaxyEntity = find(systemView);
        if(galaxyEntity.isPresent()) {
            galaxyEntity.get().updated = LocalDateTime.now();
            saveOrUpdate(galaxyEntity.get());
        } else {
            GalaxyEntity entity = new GalaxyEntity();
            entity.universe = universeEntity;
            entity.galaxy = systemView.galaxy;
            entity.system = systemView.system;
            entity.updated = LocalDateTime.now();
            saveOrUpdate(entity);
        }
    }


    @Override
    public Optional<GalaxyEntity> find(SystemView systemView) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from GalaxyEntity " +
                    "where universe = :universe and " +
                    "       galaxy = :galaxy and " +
                    "       system = :system ", GalaxyEntity.class)
                    .setParameter("universe", universeEntity)
                    .setParameter("galaxy", systemView.galaxy)
                    .setParameter("system", systemView.system)
                    .getResultList().stream().findFirst();
        }
    }


    @Override
    public Optional<GalaxyEntity> findLatestGalaxyToView() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery(
                    "from GalaxyEntity where universe = :universe " +
                            "order by updated asc", GalaxyEntity.class)
                    .setParameter("universe", universeEntity)
                    .getResultList().stream().findFirst();
        }
    }
}
