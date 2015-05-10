package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

/**
 * Tests for MemoryDataList
 */
public class MemoryDataListTest extends TestCase {

  public void testEverything() {
    MemoryDataList list = new MemoryDataList();
    assertEquals(0, list.getDates().size());

    list.setDate(new GregorianCalendar(2010, 11, 23).getTime());
    list.setDate(new GregorianCalendar(2010, 11, 23).getTime());
    list.setDate(new GregorianCalendar(2010, 10, 23).getTime());

    assertEquals(2, list.getDates().size());
    Iterator<Date> dates = list.getDates().iterator();

    assertTrue(list.hasDate(new GregorianCalendar(2010, 10, 23).getTime()));
    assertFalse(list.hasDate(new GregorianCalendar(2012, 9, 17).getTime()));

    // should be in ascending date order.
    assertTrue(dates.hasNext());
    assertEquals(new GregorianCalendar(2010, 10, 23).getTime(), dates.next());
    assertEquals(new GregorianCalendar(2010, 11, 23).getTime(), dates.next());
    assertFalse(dates.hasNext());
  }
}
