package net.ghosttrails.www.mydetic.api;

import android.os.AsyncTask;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Non-persistent in-RAM MemoryApi implementation. Used mainly for testing.
 * Simulates the network by running background threads and (optionally) randomly
 * failing some number of them.
 */
public class InRamMemoryApi implements MemoryApi {

  /**
   * Helper class for passing results around in AsyncTask
   */
  private class AsyncResult {
    public MemoryListListener listListener;
    public SingleMemoryListener singleMemoryListener;
    public MemoryDataList memoryList;
    public MemoryData memory;
    public MyDeticException exception;
  }

  /**
   * Helper class for passing parameters to AsyncTask
   */
  private class AsyncParams {
    public MemoryListListener listListener;
    public SingleMemoryListener singleMemoryListener;
    public String userId;
    public Date memoryDate;
    public Date fromDate;
    public Date toDate;
    public MemoryData memory;
  }

  /**
   * Class for fetching the memory list asynchronously.
   */
  private class GetMemoriesTask extends AsyncTask<AsyncParams, Void,
      AsyncResult> {

    @Override
    protected AsyncResult doInBackground(AsyncParams... asyncParamses) {
      AsyncParams params = asyncParamses[0];
      AsyncResult result = new AsyncResult();

      result.listListener = params.listListener;

      simulatedSleep();
      if (checkSimulatedFail()) {
        result.exception = new MyDeticReadFailedException("Simulated");
        return result;
      }

      Map<Date, MemoryData> memoryMap = getListForUserId(params.userId);
      MemoryDataList retval = new MemoryDataList(params.userId);
      for (Date d : memoryMap.keySet()) {
        if ((params.fromDate != null) && (d.before(params.fromDate))) {
          // before specified from date.
          continue;
        }
        if ((params.toDate != null) && (d.after(params.toDate))) {
          // after toDate, so we're done.
          break;
        }
        retval.setDate(d);
      }
      result.memoryList = retval;
      return result;
    }

    @Override
    protected void onPostExecute(AsyncResult asyncResult) {
      if (asyncResult.exception != null) {
        asyncResult.listListener.onApiError(asyncResult.exception);
      } else {
        asyncResult.listListener.onApiResponse(asyncResult.memoryList);
      }
    }
  }

  /**
   * Background task to fetch a single memory
   */
  private class GetMemoryTask extends AsyncTask<AsyncParams, Void,
      AsyncResult>  {
    @Override
    protected void onPostExecute(AsyncResult asyncResult) {
      if (asyncResult.exception != null) {
        asyncResult.singleMemoryListener.onApiError(asyncResult.exception);
      } else {
        asyncResult.singleMemoryListener.onApiResponse(asyncResult.memory);
      }
    }

    @Override
    protected AsyncResult doInBackground(AsyncParams... asyncParamses) {
      AsyncParams params = asyncParamses[0];
      AsyncResult result = new AsyncResult();

      result.singleMemoryListener = params.singleMemoryListener;

      simulatedSleep();
      if (checkSimulatedFail()) {
        result.exception = new MyDeticReadFailedException("simulated");
        return result;
      }
      Map<Date, MemoryData> list = getListForUserId(params.userId);
      if (!list.containsKey(params.memoryDate)) {
        result.exception = new MyDeticNoMemoryFoundException(params.userId,
            params.memoryDate);
      }
      result.memory = list.get(params.memoryDate);
      return result;
    }
  }

  /**
   * Background task to fetch a single memory
   */
  private class PutMemoryTask extends AsyncTask<AsyncParams, Void,
      AsyncResult>  {
    @Override
    protected void onPostExecute(AsyncResult asyncResult) {
      if (asyncResult.exception != null) {
        asyncResult.singleMemoryListener.onApiError(asyncResult.exception);
      } else {
        asyncResult.singleMemoryListener.onApiResponse(asyncResult.memory);
      }
    }

    @Override
    protected AsyncResult doInBackground(AsyncParams... asyncParamses) {
      AsyncParams params = asyncParamses[0];
      AsyncResult result = new AsyncResult();

      result.singleMemoryListener = params.singleMemoryListener;

      simulatedSleep();
      if (checkSimulatedFail()) {
        result.exception = new MyDeticWriteFailedException("simulated");
        return result;
      }

      Map<Date, MemoryData> list = getListForUserId(params.userId);
      list.put(params.memory.getMemoryDate(), (MemoryData) params.memory.clone());
      result.memory = list.get(params.memory.getMemoryDate());

      return result;
    }
  }

  /**
   * Background task to fetch a single memory
   */
  private class DeleteMemoryTask extends AsyncTask<AsyncParams, Void,
      AsyncResult>  {
    @Override
    protected void onPostExecute(AsyncResult asyncResult) {
      if (asyncResult.exception != null) {
        asyncResult.singleMemoryListener.onApiError(asyncResult.exception);
      } else {
        asyncResult.singleMemoryListener.onApiResponse(asyncResult.memory);
      }
    }

    @Override
    protected AsyncResult doInBackground(AsyncParams... asyncParamses) {
      AsyncParams params = asyncParamses[0];
      AsyncResult result = new AsyncResult();

      result.singleMemoryListener = params.singleMemoryListener;

      simulatedSleep();
      if (checkSimulatedFail()) {
        result.exception = new MyDeticWriteFailedException("simulated");
        return result;
      }

      Map<Date, MemoryData> list = getListForUserId(params.userId);
      if (!list.containsKey(params.memoryDate)) {
        result.exception =
            new MyDeticNoMemoryFoundException(params.userId, params.memoryDate);
      } else {
        result.memory = list.remove(params.memoryDate);
      }
      return result;
    }
  }

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
   * @param userId   the user id.
   * @param listener The callback to receive the results.
   */
  @Override
  public void getMemories(String userId, MemoryListListener listener) {
    getMemories(userId, null, null, listener);
  }

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either
   * date can be null, which indicates no bound on the range in that direction.
   *
   * @param userId   the user id.
   * @param fromDate the earliest date to include memories from.
   * @param toDate   the latest date to include memories from.
   * @param listener callback for results.
   */
  @Override
  public void getMemories(String userId, Date fromDate, Date toDate,
                          MemoryListListener listener) {
    AsyncParams params = new AsyncParams();
    params.listListener = listener;
    params.userId = userId;
    params.fromDate = fromDate;
    params.toDate = toDate;
    new GetMemoriesTask().execute(params);
  }

  /**
   * @param userId     the user id
   * @param memoryDate the date to get a memory for
   * @param listener callback for results.
   */
  @Override
  public void getMemory(String userId, Date memoryDate,
                        SingleMemoryListener listener) {
    AsyncParams params = new AsyncParams();
    params.singleMemoryListener = listener;
    params.userId = userId;
    params.memoryDate = memoryDate;
    new GetMemoryTask().execute(params);
  }

  /**
   * Adds or updates a memory
   *
   * @param userId
   * @param memory
   */
  @Override
  public void putMemory(String userId, MemoryData memory,
                        SingleMemoryListener listener) {
    AsyncParams params = new AsyncParams();
    params.singleMemoryListener = listener;
    params.userId = userId;
    params.memory = memory;
    new PutMemoryTask().execute(params);
  }

  /**
   * @param userId
   * @param memoryDate
   */
  @Override
  public void deleteMemory(String userId, Date memoryDate,
                           SingleMemoryListener listener) {
    AsyncParams params = new AsyncParams();
    params.singleMemoryListener = listener;
    params.userId = userId;
    params.memoryDate = memoryDate;
    new DeleteMemoryTask().execute(params);
  }

  /**
   * Testing method to populate data.
   *
   * @param userId
   * @param memories
   */
  public void populateMemories(String userId, List<MemoryData> memories) {
    Map<Date, MemoryData> list = getListForUserId(userId);
    for (MemoryData memory: memories) {
      list.put(memory.getMemoryDate(), (MemoryData)memory.clone());
    }
  }

  /**
   * Testing method to clear data
   * @param userId
   */
  public void clearMemories(String userId) {
    Map<Date, MemoryData> list = getListForUserId(userId);
    list.clear();
  }

}
