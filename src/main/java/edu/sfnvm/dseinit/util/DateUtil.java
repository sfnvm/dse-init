package edu.sfnvm.dseinit.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private DateUtil() {
    }

    public static Instant parseStringToUtcInstant(String instantStr) {
        return LocalDateTime
            .parse(instantStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .atZone(ZoneId.of("Z")).toInstant();
    }
}
