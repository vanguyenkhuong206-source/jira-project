// DateUtil.java
package com.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(FORMATTER);
    }
}