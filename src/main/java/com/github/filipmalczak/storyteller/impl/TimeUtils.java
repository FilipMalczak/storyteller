package com.github.filipmalczak.storyteller.impl;

import java.time.*;

public class TimeUtils {
    public static final ZoneId runtimeZone = ZoneId.systemDefault();
    public static final ZoneId utc = ZoneOffset.UTC.normalized();

    public static ZonedDateTime fromTimestamp(long timestamp){
        var instant = Instant.ofEpochSecond(timestamp);
        var local = LocalDateTime.ofInstant(instant, utc);
        var runtime = local.atZone(runtimeZone);
        return runtime;
    }

    public static long toTimestamp(ZonedDateTime dateTime){
        var inUtc = dateTime.withZoneSameInstant(utc);
        var timestamp = inUtc.toEpochSecond();
        return timestamp;
    }
}
