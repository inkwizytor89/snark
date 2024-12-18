package org.enoch.snark.common;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.ThreadMap;

public class Debug {

    public static void log(String name, String message) {
        ThreadMap threadMap = Instance.getGlobalMainConfigMap(name);
        if(threadMap.isEmpty()) threadMap = Instance.getGlobalMainConfigMap();
        boolean debug = threadMap.getConfigBoolean(ThreadMap.DEBUG, false);
        if(debug) System.err.println(name+": "+message);
    }
}
