package org.enoch.snark.instance.si.module.building;

import org.enoch.snark.instance.si.module.building.list.AbstractBuildingList;

import java.util.*;

import static org.enoch.snark.instance.si.module.ThreadMap.ARRAY_SEPARATOR;


public class BuildingManager {
    private static final Map<String, List<BuildRequest>> cachedLists = new HashMap<>();

    public static Queue<BuildRequest> getBuildRequests(String names, boolean debug) {
        if(!cachedLists.containsKey(names)) {
            List<String> listNames = Arrays.asList(names.split(ARRAY_SEPARATOR));
            cachedLists.put(names, AbstractBuildingList.convert(listNames));
            if(debug) {
                System.err.println("Created list: " + names);
                cachedLists.get(names).forEach(System.err::println);
                System.err.println();
            }
        }
        return new LinkedList<>(cachedLists.get(names));
    }
}
