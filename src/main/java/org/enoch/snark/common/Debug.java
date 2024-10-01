package org.enoch.snark.common;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.ConfigMap;

public class Debug {

    public static void log(String name, String message) {
        ConfigMap configMap = Instance.getMainConfigMap(name);
        if(configMap.isEmpty()) configMap = Instance.getMainConfigMap();
        boolean debug = configMap.getConfigBoolean(ConfigMap.DEBUG, false);
        if(debug) System.err.println(name+": "+message);
    }
}
