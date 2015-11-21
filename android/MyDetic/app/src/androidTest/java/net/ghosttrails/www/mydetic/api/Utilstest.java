package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import net.ghosttrails.www.mydetic.api.Utils;

import org.joda.time.LocalDate;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrewr on 24/04/15.
 */
public class Utilstest extends TestCase {

    public static Calendar dateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public void testParseIso() throws ParseException {
        LocalDate d1 = Utils.parseIsoDate("2014-11-12");
        assertEquals(11, d1.getMonthOfYear());
        assertEquals(12, d1.getDayOfMonth());
        assertEquals(2014, d1.getYear());
    }

    public void testParseIsoFail() {
        try {
            LocalDate d1 = Utils.parseIsoDate("20tr14-11-12");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Exception should have been thrown
        }
    }
}
