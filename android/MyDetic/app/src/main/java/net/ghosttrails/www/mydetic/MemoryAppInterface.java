package net.ghosttrails.www.mydetic;

import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Interface for classes that manage memory data.
 */
public interface MemoryAppInterface {

    public MemoryApi getApi();

    public void setApi(MemoryApi api);

    public MemoryDataList getMemories();

    public void setMemories(MemoryDataList memories);

    public MemoryData getCachedMemory(LocalDate d);

    public void setCachedMemory(MemoryData memoryData) throws MyDeticException;

    public void clearMemoryCache();

    public MyDeticConfig getConfig();

    public void setConfig(MyDeticConfig config);

}
