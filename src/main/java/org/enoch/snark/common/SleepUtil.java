package org.enoch.snark.common;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SleepUtil {

    public static void pause() {
        pause(1);
    }
    public static void pause(int i) {
        sleep(MILLISECONDS, i * 200);
    }
    public static void sleep() {
        secondsToSleep(1);
    }
    public static void secondsToSleep(int seconds) {
        sleep(SECONDS, seconds);
    }

    public static void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
