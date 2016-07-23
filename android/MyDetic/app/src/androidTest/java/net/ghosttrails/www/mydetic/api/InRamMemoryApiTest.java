package net.ghosttrails.www.mydetic.api;

import junit.framework.TestCase;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;

import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Unit Tests for InRamMemoryApi
 */
public class InRamMemoryApiTest extends TestCase {

  private static long BLOCKING_TIMEOUT_MS = 2000;

  private InRamMemoryApi api;
  private String userId = "theUserID";
  private LocalDate date = new LocalDate(2014, 5, 3);
  private boolean isApiCallInFlight = false;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.api = new InRamMemoryApi();
    this.isApiCallInFlight = false;
  }

  private void waitForApiCall() {
    // Wait for put to finish or time out
    long startTime = System.currentTimeMillis();
    while (isApiCallInFlight
        && ((System.currentTimeMillis() - startTime) < BLOCKING_TIMEOUT_MS)) {
      try {
        Thread.sleep(100, 0);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  /**
   * Helper to add a memory and block on results.
   *
   * @param userId
   * @param memory
   */
  private void blockingMemoryPut(String userId,
                                 MemoryData memory) throws Exception {
    if (isApiCallInFlight) {
      throw new Exception("blockingMemoryPut called when isApiCallInFlight is true");
    }
    isApiCallInFlight = true;
    api.putMemory(userId, memory, new MemoryApi.SingleMemoryListener() {
      @Override
      public void onApiResponse(MemoryData memory) {
        isApiCallInFlight = false;
      }

      @Override
      public void onApiError(MyDeticException exception) {
        isApiCallInFlight = false;
      }
    });
    waitForApiCall();
  }

  private MemoryData blockingMemoryGet(String userId,
                                       LocalDate memoryDate) throws Exception {
    if (isApiCallInFlight) {
      throw new Exception("blockingMemoryGet called when isApiCallInFlight is true");
    }
    isApiCallInFlight = true;
    final MemoryData[] retval = {null};
    api.getMemory(userId, memoryDate, new MemoryApi.SingleMemoryListener() {
      @Override
      public void onApiResponse(MemoryData memory) {
        retval[0] = memory;
        isApiCallInFlight = false;
      }

      @Override
      public void onApiError(MyDeticException exception) {
        isApiCallInFlight = false;
      }
    });

    waitForApiCall();
    return retval[0];
  }

  private MemoryData blockingMemoryDelete(String userId,
                                          LocalDate memoryDate) throws Exception {
    if (isApiCallInFlight) {
      throw new Exception("blockingMemoryGet called when isApiCallInFlight is true");
    }
    isApiCallInFlight = true;
    final MemoryData[] retval = {null};
    api.deleteMemory(userId, memoryDate, new MemoryApi.SingleMemoryListener() {
      @Override
      public void onApiResponse(MemoryData memory) {
        retval[0] = memory;
        isApiCallInFlight = false;
      }

      @Override
      public void onApiError(MyDeticException exception) {
        isApiCallInFlight = false;
      }
    });

    waitForApiCall();
    return retval[0];
  }


  private MemoryDataList blockingMemoriesGet(String userId) throws Exception {
    return blockingMemoriesGet(userId, null, null);
  }

  private MemoryDataList blockingMemoriesGet(String userId,
                                             LocalDate fromDate,
                                             LocalDate toDate) throws Exception {
    if (isApiCallInFlight) {
      throw new Exception("blockingMemoriesGet called when isApiCallInFlight is true");
    }
    isApiCallInFlight = true;
    final MemoryDataList[] retval = {null};
    api.getMemories(userId, fromDate, toDate, new MemoryApi.MemoryListListener() {
      @Override
      public void onApiResponse(MemoryDataList memories) {
        retval[0] = memories;
        isApiCallInFlight = false;
      }

      @Override
      public void onApiError(MyDeticException exception) {
        isApiCallInFlight = false;
      }
    });

    waitForApiCall();
    return retval[0];
  }


  public void testAddAndRetrieve() throws Exception {

    assertEquals("Should have no memories", 0, blockingMemoriesGet(userId).getDates().size());

    blockingMemoryPut(userId, new MemoryData(userId, "a memory", date));
    assertTrue(blockingMemoryGet(userId, date) != null);

    MemoryDataList list = blockingMemoriesGet(userId);
    assertEquals("Should have one memory", 1, list.getDates().size());
    assertTrue(list.hasDate(date));

    MemoryData md = blockingMemoryGet(userId, date);
    assertNotNull(md);
    assertEquals(userId, md.getUserId());
    assertEquals(date, md.getMemoryDate());
    assertEquals("a memory", md.getMemoryText());

    assertNull(blockingMemoryGet("different user", date));
  }

  public void testUpdate() throws Exception {
    blockingMemoryPut(userId, new MemoryData(userId, "a memory", date));
    MemoryData md = blockingMemoryGet(userId, date);
    assertEquals("a memory", md.getMemoryText());

    blockingMemoryPut(userId, new MemoryData(userId, "another memory", date));
    md = blockingMemoryGet(userId, date);
    assertEquals("another memory", md.getMemoryText());
  }

  public void testRetrieveRange() throws Exception {

    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-01", new LocalDate(2014, 5, 1)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-02", new LocalDate(2014, 5, 2)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-03", new LocalDate(2014, 5, 3)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-04", new LocalDate(2014, 5, 4)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-05", new LocalDate(2014, 5, 5)));

    assertEquals(5, blockingMemoriesGet(userId).getDates().size());
    assertEquals(5, blockingMemoriesGet(userId, null, null).getDates().size());
    assertEquals(4, blockingMemoriesGet(userId, new LocalDate(2014, 5, 2), null).getDates().size());
    assertEquals(5, blockingMemoriesGet(userId, new LocalDate(2014, 5, 1), null).getDates().size());
    assertEquals(5, blockingMemoriesGet(userId, null, new LocalDate(2014, 5, 5)).getDates().size());
    assertEquals(4, blockingMemoriesGet(userId, null, new LocalDate(2014, 5, 4)).getDates().size());
    assertEquals(3, blockingMemoriesGet(userId, new LocalDate(2014, 5, 2), new LocalDate(2014, 5, 4)).getDates().size());
    assertEquals(1, blockingMemoriesGet(userId, new LocalDate(2014, 5, 3), new LocalDate(2014, 5, 3)).getDates().size());
  }

  public void testDelete() throws Exception {
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-01", new LocalDate(2014, 5, 1)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-02", new LocalDate(2014, 5, 2)));
    blockingMemoryPut(userId, new MemoryData(userId, "2014-05-03", new LocalDate(2014, 5, 3)));

    assertEquals(3, blockingMemoriesGet(userId).getDates().size());
    blockingMemoryDelete(userId, new LocalDate(2014, 5, 1));
    assertEquals(2, blockingMemoriesGet(userId).getDates().size());
    assertNull(blockingMemoryGet(userId, new LocalDate(2014, 5, 1)));

    assertNull(blockingMemoryDelete(userId, new LocalDate(2014, 5, 1)));
  }
}
