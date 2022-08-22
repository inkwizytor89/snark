package org.enoch.snark.instance;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void sleep() {
        secondsToSleep(1L);
    }
    public static void secondsToSleep(Long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
