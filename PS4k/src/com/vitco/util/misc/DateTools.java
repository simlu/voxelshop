package com.vitco.util.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * some helpful functions regarding date/time
 */
public class DateTools {
    // helper to get time string
    public static String now(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        return dateFormat.format(date);
    }
}
