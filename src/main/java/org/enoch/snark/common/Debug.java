package org.enoch.snark.common;

import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;

public class Debug {

    public static void log(AbstractThread thread, String message) {
        ThreadMap threadMap = thread.map();
        boolean debug = threadMap.getConfigBoolean(ThreadMap.DEBUG, false);
        if(debug) System.err.println(threadMap.name()+": "+message);
    }
}
