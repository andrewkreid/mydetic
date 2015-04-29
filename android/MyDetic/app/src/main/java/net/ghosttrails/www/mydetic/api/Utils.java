package net.ghosttrails.www.mydetic.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generic utility functions for working with Memories
 */
public class Utils {

    static DateFormat isoDf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Parse an ISO format date (YYYY-MM-DD) into a java.util.Date
     *
     * @param isostr the date in "YYYY-MM-DD" format
     * @return a Date
     * @throws java.text.ParseException
     */
    public static Date parseIsoDate(String isostr) throws java.text.ParseException {
        return isoDf.parse(isostr);
    }

    public static String isoFormat(Date date) {
        return isoDf.format(date);
    }
}
