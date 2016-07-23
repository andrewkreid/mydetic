package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import org.joda.time.LocalDate;

import java.text.ParseException;

public class Utilstest extends TestCase {

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
