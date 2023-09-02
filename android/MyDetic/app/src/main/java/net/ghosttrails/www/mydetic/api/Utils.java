package net.ghosttrails.www.mydetic.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Generic utility functions for working with Memories */
public class Utils {

  static DateTimeFormatter localDateFmt = DateTimeFormatter.ISO_DATE;
  static DateTimeFormatter localDateWithDayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)");

  /**
   * Parse an ISO format date (YYYY-MM-DD) into a java.util.Date
   *
   * @param isostr the date in "YYYY-MM-DD" format
   * @return a LocalDate
   */
  public static LocalDate parseIsoDate(String isostr) throws IllegalArgumentException, DateTimeParseException {
    return LocalDate.parse(isostr, localDateFmt);
  }

  public static String isoFormat(LocalDate date) {
    return date.format(localDateFmt);
  }

  public static String isoFormatWithDay(LocalDate date) {
    return date.format(localDateWithDayFmt);
  }
}
