package net.ghosttrails.www.mydetic.api;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

public class Utilstest {

  @Test
  public void testParseIso() {
    LocalDate d1 = Utils.parseIsoDate("2014-11-12");
    assertEquals(11, d1.getMonthOfYear());
    assertEquals(12, d1.getDayOfMonth());
    assertEquals(2014, d1.getYear());
  }

  @Test
  public void testParseIsoFail() {
    try {
      LocalDate d1 = Utils.parseIsoDate("20tr14-11-12");
      fail("Should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Exception should have been thrown
    }
  }
}
