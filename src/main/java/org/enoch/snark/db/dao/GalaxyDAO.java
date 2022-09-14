package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.model.SystemView;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GalaxyDAO extends AbstractDAO<GalaxyEntity> {

    private static GalaxyDAO INSTANCE;

    private GalaxyDAO() {
        super();
    }

    public static GalaxyDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GalaxyDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<GalaxyEntity> getEntitylass() {
        return GalaxyEntity.class;
    }

    public void update(SystemView systemView) {
        final Optional<GalaxyEntity> galaxyEntity = find(systemView);
        if(galaxyEntity.isPresent()) {
            galaxyEntity.get().updated = LocalDateTime.now();
            saveOrUpdate(galaxyEntity.get());
        } else {
            GalaxyEntity entity = new GalaxyEntity();
            entity.galaxy = systemView.galaxy;
            entity.system = systemView.system;
            entity.updated = LocalDateTime.now();
            saveOrUpdate(entity);
        }
    }

    public Optional<GalaxyEntity> find(SystemView systemView) {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from GalaxyEntity " +
                    "where galaxy = :galaxy and " +
                    "       system = :system ", GalaxyEntity.class)
                    .setParameter("galaxy", systemView.galaxy)
                    .setParameter("system", systemView.system)
                    .getResultList().stream().findFirst();
        }
    }

    public Optional<GalaxyEntity> findLatestGalaxyToView() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery(
                    "from GalaxyEntity order by updated asc", GalaxyEntity.class)
                    .getResultList().stream().findFirst();
        }
    }

    public List<GalaxyEntity> findLatestGalaxyToView(int count) {
        synchronized (JPAUtility.dbSynchro) {
            List<GalaxyEntity> resultList = entityManager.createQuery(
                    "from GalaxyEntity order by updated asc", GalaxyEntity.class)
                    .getResultList();
            if(resultList.isEmpty())    return new ArrayList<>();
            return resultList.subList(0, count);
        }
    }

    public List<GalaxyEntity> findNotExplored() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery(
                    "from GalaxyEntity where updated = null", GalaxyEntity.class)
                    .getResultList();
        }
    }

    public void persistGalaxyMap(int galaxyCount, int systemCount) {
        synchronized (JPAUtility.dbSynchro) {
            entityManager.getTransaction().begin();
            for (int i = 1; i <= galaxyCount; i++) {
                for (int j = 1; j <= systemCount; j++) {
                    GalaxyEntity galaxyEntity = new GalaxyEntity();
                    galaxyEntity.galaxy = i;
                    galaxyEntity.system = j;
                    galaxyEntity.updated = null;
                    entityManager.persist(galaxyEntity);
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.getTransaction().commit();
        }
    }
}
