package org.enoch.snark.db.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtility {
    private static final EntityManagerFactory emFactory;
    private static EntityManager entityManager;
    static {
        emFactory = Persistence.createEntityManagerFactory("PERSISTENCE");
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
