package net.ghosttrails.www.mydetic.api;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Generic utility functions for working with Memories
 */
public class Utils {

    static DateTimeFormatter localDateFmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    static DateTimeFormatter localDateWithDayFmt = DateTimeFormat.forPattern("yyyy-MM-dd (EEE)");

    /**
     * Parse an ISO format date (YYYY-MM-DD) into a java.util.Date
     *
     * @param isostr the date in "YYYY-MM-DD" format
     * @return a LocalDate
     * @throws java.text.ParseException
     */
    public static LocalDate parseIsoDate(String isostr) throws IllegalArgumentException {
        return localDateFmt.parseLocalDate(isostr);
    }

    public static String isoFormat(LocalDate date) {
        return localDateFmt.print(date);
    }

    public static String isoFormatWithDay(LocalDate date) {
        return localDateWithDayFmt.print(date);
    }

}
