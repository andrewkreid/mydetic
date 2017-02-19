package net.ghosttrails.www.mydetic;

import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.joda.time.LocalDate;

/**
 * Interface for classes that manage memory data.
 */
public interface MemoryAppInterface {

    MemoryApi getApi();

    void setApi(MemoryApi api);

    MemoryDataList getMemories();

    void setMemories(MemoryDataList memories);

    MemoryData getCachedMemory(LocalDate d);

    void setCachedMemory(MemoryData memoryData) throws MyDeticException;

    void clearMemoryCache();

    MyDeticConfig getConfig();

    void setConfig(MyDeticConfig config);

}
