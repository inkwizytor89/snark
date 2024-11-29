package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.ConfigMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtility {
    private static EntityManagerFactory emFactory;
    private static EntityManager entityManager;
    public static final Object dbSynchro = new Object();
    public static String syncMethod;

    public static final String H2_PERSISTENCE = "H2-PERSISTENCE";

    public static final String H2_URL = "jdbc:h2:file:./db/snark;MODE=PostgreSQL";

    public static void buildDefaultEntityManager(String persistence) {
        if(emFactory != null) {
            emFactory.close();
        }
        emFactory = Persistence.createEntityManagerFactory(persistence);
        if(entityManager != null) {
            entityManager.close();
        }
        entityManager = emFactory.createEntityManager();
    }

    public static EntityManager getEntityManager(){
        if(entityManager == null) {
            entityManager =  createEntityManager();
        }
        return entityManager;
    }

    public static EntityManager createEntityManager(){
        EntityManagerFactory managerFactory;
        Map<String, String> persistenceMap = new HashMap<>();

        persistenceMap.put("javax.persistence.jdbc.url", "jdbc:postgresql://localhost/"+ Instance.getGlobalMainConfigMap().getConfig(ConfigMap.SERVER));

        managerFactory = Persistence.createEntityManagerFactory("default", persistenceMap);
        return  managerFactory.createEntityManager();
    }

    public static void close(){
        emFactory.close();
    }
}
