package org.enoch.snark.expression.global;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GlobalExpressionCache {

    private final Map<String, List<CacheEntry<?>>> entriesByKey = new HashMap<>();

    static final class CacheEntry<T> {
        final T value;
        final Instant expiresAt;
        final List<String> keys;

        CacheEntry(T value, Instant expiresAt, List<String> keys) {
            this.value = value;
            this.expiresAt = expiresAt;
            this.keys = keys;
        }

        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    public synchronized boolean containsValid(String key) {
        return findValidEntry(key) != null;
    }

    synchronized CacheEntry<?> get(String key) {
        return findValidEntry(key);
    }

    public synchronized void put(String key, Object value, Duration ttl, String... invalidationKeys) {
        removeEntriesForKey(key);

        Set<String> keys = new LinkedHashSet<>();
        keys.add(key);
        for (String invalidationKey : invalidationKeys) {
            if (invalidationKey != null && !invalidationKey.isBlank()) {
                keys.add(invalidationKey);
            }
        }

        CacheEntry<?> entry = new CacheEntry<>(value, Instant.now().plus(ttl), List.copyOf(keys));
        for (String entryKey : entry.keys) {
            entriesByKey.computeIfAbsent(entryKey, ignored -> new ArrayList<>()).add(entry);
        }
    }

    public synchronized int invalidateByKey(String key) {
        return removeEntriesForKey(key);
    }

    public synchronized int clearAll() {
        int size = getSize();
        entriesByKey.clear();
        return size;
    }

    public synchronized int getSize() {
        Set<CacheEntry<?>> entries = new HashSet<>();
        entriesByKey.values().forEach(entries::addAll);
        return entries.size();
    }

    private CacheEntry<?> findValidEntry(String key) {
        List<CacheEntry<?>> entries = entriesByKey.get(key);
        if (entries == null) {
            return null;
        }

        CacheEntry<?> validEntry = null;
        for (CacheEntry<?> entry : List.copyOf(entries)) {
            if (entry.isValid()) {
                if (validEntry == null) {
                    validEntry = entry;
                }
            } else {
                removeEntry(entry);
            }
        }
        return validEntry;
    }

    private int removeEntriesForKey(String key) {
        List<CacheEntry<?>> entries = entriesByKey.get(key);
        if (entries == null) {
            return 0;
        }

        Set<CacheEntry<?>> uniqueEntries = new HashSet<>(entries);
        uniqueEntries.forEach(this::removeEntry);
        return uniqueEntries.size();
    }

    private void removeEntry(CacheEntry<?> entry) {
        for (String key : entry.keys) {
            List<CacheEntry<?>> entries = entriesByKey.get(key);
            if (entries != null) {
                entries.remove(entry);
                if (entries.isEmpty()) {
                    entriesByKey.remove(key);
                }
            }
        }
    }
}
