package org.enoch.snark.db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class QueryService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<String> runString(String query) {
        // Rozbijamy po "|"
        String[] parts = query.split("\\|", 3);
        if (parts.length == 1) {
            return Collections.singletonList(parts[0]);
        }if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid query format. Expected: Class|SQL|params");
        }

        String className = parts[0];
        String sql = parts[1];
        String[] args = {};
        if (parts.length == 3 && !parts[2].isBlank()) {
            args = parts[2].split(",");
        }

        try {
            Class<?> clazz = Class.forName("org.enoch.snark.db.entity."+className+"Entity");
            return runQuery(clazz, sql, args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + className, e);
        }
    }


    public List<String> runQuery(Class clazz, String sql, String... args) {
        Query nativeQuery = entityManager.createNativeQuery(sql, clazz);
        for (int i = 0; i < args.length; i++) {
            nativeQuery.setParameter(i + 1, args[i]);
        }
        List resultList = nativeQuery.getResultList().stream()
                .map(Object::toString)
                .toList();
        return resultList;
    }
}
