package org.enoch.snark.common;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {

    public static <K,V> void updateMapKeys(Map<K,V> map, List<K> newKeys, V defaultValue){
        if(newKeys.isEmpty()) return;
        Set<K> oldKeys = map.keySet();
        List<K> toRemove = oldKeys.stream().filter(o -> !newKeys.contains(o)).collect(Collectors.toList());
        toRemove.forEach(map::remove);
        List<K> toAdd = newKeys.stream().filter(o -> !oldKeys.contains(o)).collect(Collectors.toList());
        toAdd.forEach(o -> map.put(o, defaultValue));
    }
}
