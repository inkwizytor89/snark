package org.enoch.snark;

import org.enoch.snark.db.dao.impl.UniverseDAOImpl;
import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.instance.Instance;

import javax.persistence.EntityManager;

public class Test {

    public static final String PLAY_TEXT = "Graj";

    public static void main(String[] args) {
//        final EntityManager entityManager = JPAUtility.getEntityManager();
//        UniverseDAOImpl universeDAO = new UniverseDAOImpl(entityManager);
//        for(UniverseEntity universeEntity : universeDAO.fetchAllUniverses()) {
//            if(universeEntity.name.equals("Fenrir")){
//                Runnable task = new Instance(universeEntity)::runTest;
//                new Thread(task).start();
//            }
//        }
    }
}
