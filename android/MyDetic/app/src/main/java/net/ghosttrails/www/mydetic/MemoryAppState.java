package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.ghosttrails.www.mydetic.api.InRamMemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.RestfulMemoryApi;
import net.ghosttrails.www.mydetic.api.SampleSetPopulator;
import net.ghosttrails.www.mydetic.cachedb.MyDeticSQLDBContract;
import net.ghosttrails.www.mydetic.cachedb.MyDeticSQLDBHelper;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.HashMap;

/**
 * Singleton that stores application state.
 */
public class MemoryAppState implements MemoryAppInterface {

    private static MemoryAppState _state;

    private MemoryApi api;
    private MemoryDataList memories;
    private MyDeticConfig config;
    private MyDeticSQLDBHelper cacheDbHelper;
    private SQLiteDatabase dbHandle;

    /**
     * in-RAM cache of memories we've downloaded already
     */
    private HashMap<LocalDate, MemoryData> memoryCache;

    private MemoryAppState() {
        this.memoryCache = new HashMap<LocalDate, MemoryData>();
    }

    public synchronized static MemoryAppState getInstance() {
        if (_state == null) {
            _state = new MemoryAppState();
        }
        return _state;
    }

    @Override
    public MemoryApi getApi() {
        return api;
    }

    @Override
    public MemoryDataList getMemories() {
        return memories;
    }

    /**
     * Fetch a memory from the cache if one exists. As the app loads memories,
     * they are stored in this cache to avoid unnecesary network calls.
     *
     * @param d the date of the memory
     * @return a MemoryData if one has been cached, null otherwise.
     */
    @Override
    public MemoryData getCachedMemory(LocalDate d) {
        MemoryData memory = memoryCache.get(d);
        if ((memory == null) && (dbHandle != null)) {
            // Try the DB cache.
            memory = MyDeticSQLDBContract.getMemory(dbHandle, config.getUserName(),
                    config.getActiveDataStore(), d);
        }
        return memory;
    }

    @Override
    public void setCachedMemory(MemoryData memoryData) throws MyDeticException {
        if (dbHandle != null) {
            MyDeticSQLDBContract.putMemory(dbHandle, config.getActiveDataStore(), memoryData);
        }
        memoryCache.put(memoryData.getMemoryDate(), memoryData);
        memories.setDate(memoryData.getMemoryDate());
    }

    @Override
    public void clearMemoryCache() {
        memoryCache.clear();
        if (dbHandle != null) {
            MyDeticSQLDBContract.deleteMemories(dbHandle);
        }
    }

    @Override
    public MyDeticConfig getConfig() {
        return config;
    }

    @Override
    public void setApi(MemoryApi api) {
        this.api = api;
    }

    @Override
    public void setMemories(MemoryDataList memories) {
        this.memories = memories;
    }

    @Override
    public void setConfig(MyDeticConfig config) {
        this.config = config;
    }

    public MyDeticSQLDBHelper getCacheDbHelper() {
        return cacheDbHelper;
    }

    public void setCacheDbHelper(MyDeticSQLDBHelper cacheDbHelper) {
        this.cacheDbHelper = cacheDbHelper;
    }

    public SQLiteDatabase getDbHandle() {
        return dbHandle;
    }

    public void setDbHandle(SQLiteDatabase dbHandle) {
        this.dbHandle = dbHandle;
    }

    public void reloadMemories(final Context context) {
        reloadMemories(context, new MemoryApi.MemoryListListener() {
            @Override
            public void onApiResponse(MemoryDataList memories) {
                // empty callback
            }

            @Override
            public void onApiError(MyDeticException exception) {
                // empty callback
            }
        });
    }

    public void reloadMemories(final Context context, final MemoryApi.MemoryListListener externalListener) {
        getApi().getMemories(config.getUserName(), new MemoryApi.MemoryListListener() {
            @Override
            public void onApiResponse(MemoryDataList newMemories) {
                getMemories().clear();
                try {
                    getMemories().mergeFrom(newMemories);
                } catch (MyDeticException e) {
                    AppUtils.smallToast(context, e.getMessage());
                }
                externalListener.onApiResponse(getMemories());
            }

            @Override
            public void onApiError(MyDeticException e) {
                AppUtils.smallToast(context, e.getMessage());
                Log.e("MyDetic", e.getMessage());
                externalListener.onApiError(e);
            }
        });

    }

    /**
     * Update the API and user params from the config (eg when it has changed).
     * Clear existing memory data in RAM.
     */
    public void refreshSettingsFromConfig(Context context) {
        // Init the api and memory list
        if (config == null) {
            return;
        }
        String userId = config.getUserName();
        if (config.getActiveDataStore().equals(MyDeticConfig.DS_INRAM)) {
            InRamMemoryApi ramApi = new InRamMemoryApi();
            ramApi.setSimulatedDelayMs(1000);
            ramApi.setSimulatedFailureRate(0);
            setApi(ramApi);
            try {
                SampleSetPopulator.populateTestSet(ramApi, userId, true);
            } catch (MyDeticException e) {
                Log.e("MyDeticApplication", "Sample Populate Failed", e);
            }
        } else if (config.getActiveDataStore().equals(MyDeticConfig.DS_RESTAPI)) {
            setApi(new RestfulMemoryApi(context, config));
        }

        MemoryDataList memories = new MemoryDataList();
        memories.setUserID(userId);
        setMemories(memories);
    }


}
