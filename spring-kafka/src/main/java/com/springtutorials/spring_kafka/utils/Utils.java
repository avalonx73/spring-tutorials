package com.springtutorials.spring_kafka.utils;


import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

public class Utils {

    @SneakyThrows
    public static void sleepMiliseconds(long timeout) {
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
    @SneakyThrows
    public static void sleepSeconds(long timeout) {
        TimeUnit.SECONDS.sleep(timeout);
    }

    @SneakyThrows
    public static void sleepMinutes(long timeout) {
        TimeUnit.MINUTES.sleep(timeout);
    }
}
