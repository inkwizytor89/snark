package org.enoch.snark.common;

import org.enoch.snark.common.time.Duration;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SleepUtil {

    public static void pause() {
        pause(1L);
    }
    @Deprecated
    public static void pause(int i) {
        sleep(MILLISECONDS, i * 200);
    }

    public static void pause(long i) {
        sleep(MILLISECONDS, i * 200);
    }
    public static void sleep() {
        secondsToSleep(1L);
    }
    @Deprecated
    public static void secondsToSleep(int seconds) {
        sleep(SECONDS, seconds);
    }

    public static void secondsToSleep(long seconds) {
        sleep(SECONDS, seconds);
    }

    public static void sleep(Duration duration) {
        sleep(SECONDS, duration.getSeconds());
    }

    public static void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void sleep(TimeUnit timeUnit, long i) {
        try {
            timeUnit.sleep(i);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
