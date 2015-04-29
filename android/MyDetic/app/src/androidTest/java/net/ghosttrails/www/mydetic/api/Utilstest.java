package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import net.ghosttrails.www.mydetic.api.Utils;

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
        Date d1 = Utils.parseIsoDate("2014-11-12");
        Calendar cal = dateToCalendar(d1);
        assertEquals(10, cal.get(Calendar.MONTH));
        assertEquals(12, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(2014, cal.get(Calendar.YEAR));
    }

    public void testParseIsoFail() {
        try {
            Date d1 = Utils.parseIsoDate("20tr14-11-12");
            fail("Should have thrown a ParseException");
        } catch (ParseException e) {
            // Exception should have been thrown
        }
    }
}
