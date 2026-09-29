package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.CacheEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CacheEntryRepository extends JpaRepository<CacheEntryEntity, Long> {

    Optional<CacheEntryEntity> findByKey(String key);

    @Modifying
    @Transactional
    @Query("UPDATE CacheEntryEntity c SET c.value = 'UNKNOWN' WHERE c.key LIKE %:suffix")
    int setUnknownForSuffix(@Param("suffix") String suffix);

    default CacheEntryEntity getCacheEntryNotNull(String key) {
        Optional<CacheEntryEntity> cacheEntry = findByKey(key);
        if(cacheEntry.isPresent()) return cacheEntry.get();
        CacheEntryEntity cacheEntryEntity = new CacheEntryEntity();
        cacheEntryEntity.key = key;
        cacheEntryEntity.created = LocalDateTime.now();
        cacheEntryEntity.updated = LocalDateTime.now();
        save(cacheEntryEntity);
        return cacheEntryEntity;
    }

    default String getValue(String key) {
        Optional<CacheEntryEntity> cacheEntry = findByKey(key);
        return cacheEntry.map(cacheEntryEntity -> cacheEntryEntity.value).orElse(null);
    }

    default String getValue(String key, String defaultValue) {
        Optional<CacheEntryEntity> cacheEntry = findByKey(key);
        if(cacheEntry.isEmpty()) return defaultValue;
        return cacheEntry.get().value;
    }

    default LocalDateTime getDate(String key) {
        String value = getValue(key);
        if(value == null) return null;
        else return LocalDateTime.parse(value);
    }

    default void setValue(String key, String value) {
        CacheEntryEntity cacheEntryNotNull = getCacheEntryNotNull(key);
        cacheEntryNotNull.value = value;
        cacheEntryNotNull.updated = LocalDateTime.now();
        save(cacheEntryNotNull);
    }
}
