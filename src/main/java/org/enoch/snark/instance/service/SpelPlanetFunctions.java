package org.enoch.snark.instance.service;

import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Functions that can be used in SpEL expressions for planet data manipulation.
 * These methods are registered as SpEL functions in SpelPlanetService.
 *
 * Methods accept Object so SpEL can pass List, PlanetData or String. Strings
 * are resolved via repositories to List<PlanetData>.
 */
public class SpelPlanetFunctions {

    private static CacheEntryRepository cacheEntryRepository;
    private static ColonyRepository colonyRepository;
    private static TargetRepository targetRepository;

    public static synchronized void setRepositories(CacheEntryRepository cacheRepo, ColonyRepository colonyRepo, TargetRepository targetRepo) {
        cacheEntryRepository = cacheRepo;
        colonyRepository = colonyRepo;
        targetRepository = targetRepo;
    }

    // ...existing code...


    private static List<PlanetData> resolveToList(Object arg) {
        if (arg == null) {
            return new ArrayList<>();
        }
        if (arg instanceof List<?>) {
            List<PlanetData> out = new ArrayList<>();
            for (Object o : (List<?>) arg) {
                if (o instanceof PlanetData pd) {
                    out.add(pd);
                } else if (o != null) {
                    // If element is not PlanetData, try toString and resolve via repository
                    out.addAll(parseStringToList(o.toString()));
                }
            }
            return out;
        }
        if (arg instanceof PlanetData) {
            return new ArrayList<>(Collections.singletonList((PlanetData) arg));
        }
        // Treat everything else as String code
        return parseStringToList(arg.toString());
    }

    private static List<PlanetData> parseStringToList(String code) {
        if (code == null) {
            return new ArrayList<>();
        }
        code = code.trim();
        if (code.isEmpty()) {
            return new ArrayList<>();
        }

        if (colonyRepository == null) {
            return new ArrayList<>();
        }

        try {
            return colonyRepository.findByCode(code).stream()
                    .map(PlanetData::new)
                    .toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
