package org.enoch.snark.common;

import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Debug {

    public static void log(AbstractThread thread, String message) {
        log(thread.map(), message);
    }

    public static void log(ThreadMap threadMap, String message) {
        boolean debug = threadMap.getConfigBoolean(ThreadMap.DEBUG, false);
        if(debug) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.err.println(threadMap.name() + " [" + time + "]: " + message);
        }
    }
}
