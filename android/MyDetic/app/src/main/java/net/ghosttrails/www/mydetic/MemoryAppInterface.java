package net.ghosttrails.www.mydetic;

import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;

import java.util.Date;

/**
 * Interface for classes that manage memory data.
 */
public interface MemoryAppInterface {

  public MemoryApi getApi();

  public MemoryDataList getMemories();

  public MemoryData getCachedMemory(Date d);

  public void setCachedMemory(MemoryData memoryData);

  public MyDeticConfig getConfig();
}
