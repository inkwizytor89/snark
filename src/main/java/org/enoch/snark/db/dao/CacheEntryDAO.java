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

    public CacheEntryEntity getCacheEntry(String key) {
        synchronized (JPAUtility.dbSynchro) {
            List<CacheEntryEntity> cacheEntryEntities = entityManager.createQuery("" +
                    "from CacheEntryEntity where key = :key", CacheEntryEntity.class)
                    .setParameter("key", key)
                    .getResultList();
            if(cacheEntryEntities.isEmpty()) return null;
            return cacheEntryEntities.get(0);
        }
    }

    public CacheEntryEntity getCacheEntryNotNull(String key) {
        CacheEntryEntity cacheEntry = getCacheEntry(key);
        if(cacheEntry == null) {
            cacheEntry = new CacheEntryEntity();
            cacheEntry.key = key;
            cacheEntry.created = LocalDateTime.now();
            cacheEntry.updated = LocalDateTime.now();
            saveOrUpdate(cacheEntry);
        }
        return cacheEntry;
    }

    public String getValue(String key) {
        CacheEntryEntity cacheEntry = getCacheEntry(key);
        if(cacheEntry == null) return null;
        return cacheEntry.value;
    }

    public LocalDateTime getDate(String key) {
        String value = getValue(key);
        if(value == null) return null;
        else return LocalDateTime.parse(value);
    }

    public void setValue(String key, String value) {
        synchronized (JPAUtility.dbSynchro) {
            CacheEntryEntity cacheEntry = getCacheEntry(key);
            if(cacheEntry == null) {
              cacheEntry = new CacheEntryEntity();
              cacheEntry.key = key;
              cacheEntry.created = LocalDateTime.now();
            }
            cacheEntry.value = value;
            cacheEntry.updated = LocalDateTime.now();
            saveOrUpdate(cacheEntry);
        }
    }
}
