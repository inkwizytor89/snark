package org.enoch.snark.common;

import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Debug {

    public static void log(ThreadEntity threadEntity, String message) {
        boolean debug = Boolean.parseBoolean(threadEntity.debug);
        if(debug) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.err.println(threadEntity.name + " [" + time + "]: " + message);
        }
    }
}
