package net.ghosttrails.www.mydetic.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.time.LocalDate;
import org.junit.Test;

/** Tests for MemoryDataList */
public class MemoryDataListTest {

  @Test
  public void testEverything() {
    MemoryDataList list = new MemoryDataList();
    assertEquals(0, list.getDates().size());

    list.setDate(new LocalDate(2010, 11, 23));
    list.setDate(new LocalDate(2010, 11, 23));
    list.setDate(new LocalDate(2010, 10, 23));

    assertEquals(2, list.getDates().size());
    Iterator<LocalDate> dates = list.getDates().iterator();

    assertTrue(list.hasDate(new LocalDate(2010, 10, 23)));
    assertFalse(list.hasDate(new LocalDate(2012, 9, 17)));

    // should be in ascending date order.
    assertTrue(dates.hasNext());
    assertEquals(new LocalDate(2010, 10, 23), dates.next());
    assertEquals(new LocalDate(2010, 11, 23), dates.next());
    assertFalse(dates.hasNext());
  }
}
