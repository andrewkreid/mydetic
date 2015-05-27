package net.ghosttrails.www.mydetic;

import android.app.Application;
import android.util.Log;

import net.ghosttrails.www.mydetic.api.InRamMemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.SampleSetPopulator;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.util.Date;
import java.util.HashMap;

/**
 * Singleton to store global application state
 */
public class MyDeticApplication extends Application {

  private String userId;
  private MemoryApi api;
  private MemoryDataList memories;

  /** cache of memories we've downloaded already */
  private HashMap<Date, MemoryData> memoryCache;

  /**
   * Called when the application is starting, before any activity, service,
   * or receiver objects (excluding content providers) have been created.
   * Implementations should be as quick as possible (for example using
   * lazy initialization of state) since the time spent in this function
   * directly impacts the performance of starting the first activity,
   * service, or receiver in a process.
   * If you override this method, be sure to call super.onCreate().
   */
  @Override
  public void onCreate() {
    super.onCreate();
    // Init the api and memory list
    // TODO: userId from settings.
    userId = "mreynolds";
    InRamMemoryApi ramApi = new InRamMemoryApi();

    try {
      SampleSetPopulator.populateTestSet(ramApi, userId, true);
    } catch (MyDeticException e) {
      Log.e("MyDeticApplication", "Same Populate Failed", e);
    }
    ramApi.setSimulatedDelayMs(2000);
    api = ramApi;

    memories = new MemoryDataList();
    memories.setUserID(userId);

    this.memoryCache = new HashMap<Date, MemoryData>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public MemoryApi getApi() {
    return api;
  }

  public void setApi(MemoryApi api) {
    this.api = api;
  }

  public MemoryDataList getMemories() {
    return memories;
  }

  public void setMemories(
      MemoryDataList memories) {
    this.memories = memories;
  }

  /**
   * Fetch a memory from the cache if one exists. As the app loads memories,
   * they are stored in this cache to avoid unnecesary network calls.
   * @param d the date of the memory
   * @return a MemoryData if one has been cached, null otherwise.
   */
  public MemoryData getCachedMemory(Date d) {
    return memoryCache.get(d);
  }

  public void setCachedMemory(MemoryData memoryData) {
    memoryCache.put(memoryData.getMemoryDate(), memoryData);
  }
}
