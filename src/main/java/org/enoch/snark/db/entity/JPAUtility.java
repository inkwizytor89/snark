package org.enoch.snark.db.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtility {
    private static EntityManagerFactory emFactory;
    private static EntityManager entityManager;
    public static final Object dbSynchro = new Object();

    public static final String H2_PERSISTENCE = "H2-PERSISTENCE";
    public static final String H2_URL = "jdbc:h2:file:./db/snark;MODE=PostgreSQL";

    static {
        setPersistence(H2_PERSISTENCE);
    }

    public static void setPersistence(String persistence) {
        if(emFactory != null) {
            emFactory.close();
        }
        emFactory = Persistence.createEntityManagerFactory(persistence);
        if(entityManager != null) {
            entityManager.close();
        }
        entityManager = null;
    }

    public static EntityManager getEntityManager(){
        if(entityManager == null) {
            entityManager =  emFactory.createEntityManager();
        }
        return entityManager;
    }
    public static void close(){
        emFactory.close();
    }
}
