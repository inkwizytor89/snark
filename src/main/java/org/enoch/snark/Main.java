package org.enoch.snark;

import org.enoch.snark.db.dao.impl.UniverseDAOImpl;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.instance.Instance;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> unis = Arrays.asList(args);
        final EntityManager entityManager = JPAUtility.getEntityManager();

        UniverseDAOImpl universeDAO = new UniverseDAOImpl(entityManager);
        List<UniverseEntity> universes = universeDAO.fetchAllUniverses();
        for(UniverseEntity universeEntity : universes) {
            if(unis.isEmpty() || unis.contains(universeEntity.name)) {
                new Thread(new Instance(universeEntity, universeDAO)::runSI).start();
            }
        }
    }
}
