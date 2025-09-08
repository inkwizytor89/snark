package org.enoch.snark.db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class QueryService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<String> runQuery(String sql) {
        return entityManager.createNativeQuery(sql).getResultList();
    }
}
