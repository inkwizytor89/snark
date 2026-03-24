package org.enoch.snark.instance.model.expression.planet;

import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.model.expression.common.Trip;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Take next in list C:[A,B,C] -> A
 */
public class Next {

    private static ColonyRepository colonyRepository;

    public static synchronized void setRepository(ColonyRepository colonyRepo) {
        colonyRepository = colonyRepo;
    }

//    public static Object execute(PlanetData current, List<PlanetData> trip) {
//        int index = Trip.currentTripIndex(current.getPlanet(), trip);
//        return trip.get((index + 1) %  trip.size());
////        List<PlanetData> list = resolveToList(arg);
////        if (list.isEmpty()) {
////            return list;
////        }
////        List<PlanetData> result = new ArrayList<>(list.size());
////        for (int i = 1; i < list.size(); i++) {
////            result.add(list.get(i));
////        }
////        result.add(list.get(0));
////        return result;
//    }

    public static Object execute(String current,String trip) {
        List<Planet> configTrip = Planet.fromString(trip);
        int index = Trip.currentTripIndex(Planet.parse(current), configTrip);
        return configTrip.get((index + 1) %  configTrip.size());
//        List<PlanetData> list = resolveToList(arg);
//        if (list.isEmpty()) {
//            return list;
//        }
//        List<PlanetData> result = new ArrayList<>(list.size());
//        for (int i = 1; i < list.size(); i++) {
//            result.add(list.get(i));
//        }
//        result.add(list.get(0));
//        return result;
    }

//    private static List<PlanetData> resolveToList(Object arg) {
//        if (arg == null) {
//            return new ArrayList<>();
//        }
//        if (arg instanceof List<?>) {
//            List<PlanetData> out = new ArrayList<>();
//            for (Object o : (List<?>) arg) {
//                if (o instanceof PlanetData pd) {
//                    out.add(pd);
//                } else if (o != null) {
//                    // If element is not PlanetData, try toString and resolve via repository
//                    out.addAll(parseStringToList(o.toString()));
//                }
//            }
//            return out;
//        }
//        if (arg instanceof PlanetData) {
//            return new ArrayList<>(Collections.singletonList((PlanetData) arg));
//        }
//        // Treat everything else as String code
//        return parseStringToList(arg.toString());
//    }
//
//    private static List<PlanetData> parseStringToList(String code) {
//        if (code == null) {
//            return new ArrayList<>();
//        }
//        code = code.trim();
//        if (code.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        if (colonyRepository == null) {
//            return new ArrayList<>();
//        }
//
//        try {
//            return colonyRepository.findByCode(code).stream()
//                    .map(PlanetData::new)
//                    .toList();
//        } catch (Exception e) {
//            return new ArrayList<>();
//        }
//    }
}

