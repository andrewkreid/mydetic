package net.ghosttrails.www.mydetic.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Non-persistent in-RAM MemoryApi implementation. Used mainly
 * for testing.
 */
public class InRamMemoryApi implements MemoryApi {

  private Map<String, Map<Date, MemoryData>> memoryLists;

  public InRamMemoryApi() {
    memoryLists = new HashMap<String, Map<Date, MemoryData>>();
  }

  /**
   * @param userId the user id
   * @return the Map for userId. Create and return an empty one if required.
   */
  private Map<Date, MemoryData> getListForUserId(String userId) {
    if (!memoryLists.containsKey(userId)) {
      memoryLists.put(userId, new TreeMap<Date, MemoryData>());
    }
    return memoryLists.get(userId);
  }

  /**
   * @param userId the user id.
   * @return A list of all memory dates for user userId
   */
  @Override
  public MemoryDataList getMemories(String userId) {
    Map<Date, MemoryData> memoryMap = this.getListForUserId(userId);
    MemoryDataList retval = new MemoryDataList(userId);
    for (Date d : memoryMap.keySet()) {
      retval.setDate(d);
    }
    return retval;
  }

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either
   * date can be null, which indicates no bound on the range in that direction.
   *
   * @param userId   the user id.
   * @param fromDate the earliest date to include memories from.
   * @param toDate   the latest date to include memories from.
   * @return a MemoryDataList
   */
  @Override
  public MemoryDataList getMemories(String userId, Date fromDate, Date toDate) {
    Map<Date, MemoryData> memoryMap = this.getListForUserId(userId);
    MemoryDataList retval = new MemoryDataList(userId);
    for (Date d : memoryMap.keySet()) {
      if ((fromDate != null) && (d.before(fromDate))) {
        // before specified from date.
        continue;
      }
      if ((toDate != null) && (d.after(toDate))) {
        // after toDate, so we're done.
        break;
      }
      retval.setDate(d);
    }
    return retval;
  }

  /**
   * @param userId     the user id
   * @param memoryDate the date to get a memory for
   * @return a MemoryData object containing the memory for the userId and date.
   * @throws NoMemoryFoundException if no memory exists.
   */
  @Override
  public MemoryData getMemory(String userId,
                              Date memoryDate) throws NoMemoryFoundException {
    Map<Date, MemoryData> list = getListForUserId(userId);
    if (!list.containsKey(memoryDate)) {
      throw new NoMemoryFoundException(userId, memoryDate);
    }
    return list.get(memoryDate);
  }

  /**
   * Adds or updates a memory
   *
   * @param userId
   * @param memory
   * @return The added memory.
   */
  @Override
  public MemoryData putMemory(String userId, MemoryData memory) {
    Map<Date, MemoryData> list = getListForUserId(userId);
    list.put(memory.getMemoryDate(), (MemoryData) memory.clone());
    return list.get(memory.getMemoryDate());
  }

  /**
   * @param userId
   * @param memoryDate
   * @return true if there is a memory for the userId on memoryDate,
   * false otherwise.
   */
  @Override
  public boolean hasMemory(String userId, Date memoryDate) {
    Map<Date, MemoryData> list = getListForUserId(userId);
    return list.containsKey(memoryDate);
  }

  /**
   * @param userId
   * @param memoryDate
   * @return
   * @throws NoMemoryFoundException
   */
  @Override
  public MemoryData deleteMemory(String userId,
                                 Date memoryDate) throws NoMemoryFoundException {
    Map<Date, MemoryData> list = getListForUserId(userId);
    if (!list.containsKey(memoryDate)) {
      throw new NoMemoryFoundException(userId, memoryDate);
    } else {
      return list.remove(memoryDate);
    }
  }


}
