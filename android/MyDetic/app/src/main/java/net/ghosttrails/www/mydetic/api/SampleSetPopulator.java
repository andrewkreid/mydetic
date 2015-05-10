package net.ghosttrails.www.mydetic.api;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class to populate some memories
 */
public class SampleSetPopulator {

  /**
   * Create a sample set of memories for testing.
   */
  public static void populateTestSet(MemoryApi api, String userId,
                                     boolean removeExisting)
      throws NoMemoryFoundException {
    if (removeExisting) {
      MemoryDataList memories = api.getMemories(userId);
      for (Date d : memories.getDates()) {
        api.deleteMemory(userId, d);
      }
    }

    addMemory(api, userId, 2015, 4, 28, "Today was a good day");
    addMemory(api, userId, 2015, 4, 29, "Dreamt about cheese.");
    addMemory(api, userId, 2015, 4, 30,
        "Accidentally shaved off my hipster beard");
    addMemory(api, userId, 2015, 5, 3, "I don't like cabbage wih vegemite.");
    addMemory(api, userId, 2015, 5, 4, "Winter is coming.");

  }

  public static void addMemory(MemoryApi api, String userId, int year,
                               int month, int day, String memoryText) {
    Date date = new GregorianCalendar(year, month, day).getTime();
    api.putMemory(userId, new MemoryData(userId, memoryText, date));
  }

}
