package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.*;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.types.ColonyType;

import java.time.LocalDateTime;
import java.util.List;

public class CacheEntryDAO extends AbstractDAO<CacheEntryEntity> {

    private static CacheEntryDAO INSTANCE;

    private CacheEntryDAO() {
        super();
    }

    public static CacheEntryDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CacheEntryDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<CacheEntryEntity> getEntityClass() {
        return CacheEntryEntity.class;
    }

    public String getValue(String key) {
        synchronized (JPAUtility.dbSynchro) {
            CacheEntryEntity cacheEntryEntity = entityManager.createQuery("" +
                    "from CacheEntryEntity where key = :key", CacheEntryEntity.class)
                    .setParameter("key", key)
                    .getSingleResult();
            if(cacheEntryEntity == null) return null;
            return cacheEntryEntity.value;
        }
    }

    public LocalDateTime getDate(String key) {
        String value = getValue(key);
        if(value == null) return null;
        else return LocalDateTime.parse(value);
    }
}
