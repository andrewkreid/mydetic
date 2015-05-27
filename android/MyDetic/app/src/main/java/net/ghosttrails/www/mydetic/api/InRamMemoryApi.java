package net.ghosttrails.www.mydetic.api;

import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Non-persistent in-RAM MemoryApi implementation. Used mainly
 * for testing.
 */
public class InRamMemoryApi implements MemoryApi {

  private Map<String, Map<Date, MemoryData>> memoryLists;
  private int simulatedDelayMs;
  private int simulatedFailureRate;

  /**
   * used for simulated random failures
   */
  private Random random;

  public InRamMemoryApi() {
    this(0);
  }

  /**
   * @param simulatedDelayMs delay each call by this number of milliseconds.
   */
  public InRamMemoryApi(int simulatedDelayMs) {
    memoryLists = new HashMap<String, Map<Date, MemoryData>>();
    this.simulatedDelayMs = simulatedDelayMs;
    this.simulatedFailureRate = 0;
    this.random = new Random(System.currentTimeMillis());
  }

  /**
   * @return The simulated failure rate
   */
  public int getSimulatedFailureRate() {
    return simulatedFailureRate;
  }

  /**
   * Set a random rate at which API operations will fail and throw exceptions,
   * so we can test the app against a simulated unreliable service or network
   * connection.
   *
   * @param simulatedFailureRate percentage value 0-100 (int)
   */
  public void setSimulatedFailureRate(int simulatedFailureRate) {
    this.simulatedFailureRate = simulatedFailureRate;
  }

  public int getSimulatedDelayMs() {
    return simulatedDelayMs;
  }

  public void setSimulatedDelayMs(int simulatedDelayMs) {
    this.simulatedDelayMs = simulatedDelayMs;
  }

  private void simulatedSleep() {
    try {
      Thread.sleep(simulatedDelayMs);
    } catch (InterruptedException e) {
      // We don't really care if we get interrupted occasionally.
    }
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
   * @return Whether we should randomly fail this call.
   */
  private boolean checkSimulatedFail() {
    if (simulatedFailureRate > 0) {
      int randomInt = random.nextInt(100);
      return randomInt < simulatedFailureRate;
    } else {
      return false;
    }
  }

  /**
   * @param userId the user id.
   * @return A list of all memory dates for user userId
   */
  @Override
  public MemoryDataList getMemories(String userId)
      throws MyDeticReadFailedException {
    simulatedSleep();
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
    simulatedSleep();
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
   * @throws MyDeticNoMemoryFoundException if no memory exists.
   */
  @Override
  public MemoryData getMemory(String userId,
                              Date memoryDate) throws
      MyDeticNoMemoryFoundException, MyDeticReadFailedException {
    simulatedSleep();
    if(checkSimulatedFail()) {
      throw new MyDeticReadFailedException("simulated");
    }
    Map<Date, MemoryData> list = getListForUserId(userId);
    if (!list.containsKey(memoryDate)) {
      throw new MyDeticNoMemoryFoundException(userId, memoryDate);
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
    simulatedSleep();
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
    simulatedSleep();
    Map<Date, MemoryData> list = getListForUserId(userId);
    return list.containsKey(memoryDate);
  }

  /**
   * @param userId
   * @param memoryDate
   * @return
   * @throws MyDeticNoMemoryFoundException
   */
  @Override
  public MemoryData deleteMemory(String userId,
                                 Date memoryDate) throws
      MyDeticNoMemoryFoundException {
    simulatedSleep();
    Map<Date, MemoryData> list = getListForUserId(userId);
    if (!list.containsKey(memoryDate)) {
      throw new MyDeticNoMemoryFoundException(userId, memoryDate);
    } else {
      return list.remove(memoryDate);
    }
  }


}
