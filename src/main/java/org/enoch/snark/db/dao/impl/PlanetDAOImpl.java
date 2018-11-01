package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.PlanetDAO;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.UniverseEntity;

import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class PlanetDAOImpl extends AbstractDAOImpl implements PlanetDAO {

    public PlanetDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    public void saveOrUpdatePlanet(PlanetEntity planet) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        if(planet.getId() == null) {
            entityManager.persist(planet);
        } else {
            entityManager.merge(planet);
        }
        transaction.commit();
    }

    @Override
    public Optional<PlanetEntity> find(Integer galaxy, Integer system, Integer position) {
        final List<PlanetEntity> result = entityManager.createQuery("" +
                "from PlanetEntity " +
                "where universesByUniverseId = :universe and " +
                "       galaxy = :galaxy and" +
                "       system = :system and " +
                "       position = :position", PlanetEntity.class)
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
