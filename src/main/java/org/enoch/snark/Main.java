package org.enoch.snark;

import org.enoch.snark.entity.JPAUtility;
import org.enoch.snark.entity.UniversesEntity;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

public class Main {


    public static void main(String[] args) throws IOException {
        EntityManager entityManager = JPAUtility.getEntityManager();
        entityManager.getTransaction().begin();
        final List from_universe = entityManager.createQuery("from UniversesEntity", UniversesEntity.class).getResultList();
        entityManager.close();
        JPAUtility.close();
        System.out.println("Entity saved.");

    }
}
