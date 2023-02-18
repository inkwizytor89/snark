package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;

import java.util.List;

public class ColonyDAO extends AbstractDAO<ColonyEntity> {

    private static ColonyDAO INSTANCE;

    private ColonyDAO() {
        super();
    }

    public static ColonyDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ColonyDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<ColonyEntity> getEntityClass() {
        return ColonyEntity.class;
    }

    public ColonyEntity get(String code) {
        return get(new Planet(code));
    }
    public ColonyEntity get(Planet planet) {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where galaxy = :galaxy and " +
                    "system = :system and " +
                    "position = :position and " +
                    "isPlanet = :isPlanet", ColonyEntity.class)
                    .setParameter("galaxy", planet.galaxy)
                    .setParameter("system", planet.system)
                    .setParameter("position", planet.position)
                    .setParameter("isPlanet", planet.type.equals(ColonyType.PLANET))
                    .getResultList();
            if(resultList.isEmpty()) {
                System.err.println("Missing colony "+planet);
                return null;
            }
            return resultList.get(0);
        }
    }

    public ColonyEntity find(Integer cp) {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery("" +
                    "from ColonyEntity " +
                    "where cp = :cp", ColonyEntity.class)
                    .setParameter("cp", cp)
                    .getResultList();
            if(resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        }
    }

    public ColonyEntity getOldestUpdated() {
        synchronized (JPAUtility.dbSynchro) {
            List<ColonyEntity> resultList = entityManager.createQuery(
                    "from ColonyEntity " +
                            "order by updated asc", ColonyEntity.class)
                    .getResultList();
            return resultList.stream().findFirst().get();
        }
    }
}
