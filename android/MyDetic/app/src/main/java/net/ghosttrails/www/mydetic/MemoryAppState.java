package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;

import org.joda.time.LocalDate;

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
        this.memoryCache = new HashMap<>();
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
            MyDeticSQLDBContract.putMemory(dbHandle,
                    config.getActiveDataStore(), memoryData);
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

    MyDeticSQLDBHelper getCacheDbHelper() {
        return cacheDbHelper;
    }

    void setCacheDbHelper(MyDeticSQLDBHelper cacheDbHelper) {
        this.cacheDbHelper = cacheDbHelper;
    }

    void setDbHandle(SQLiteDatabase dbHandle) {
        this.dbHandle = dbHandle;
    }

    /** Load memory dates from the cached file and merge with existing dates */
    void loadMemoryDatesFromCache() throws MyDeticException {
        if (this.dbHandle == null ) {
            throw new MyDeticReadFailedException(
                    "attempted to load memories from cache before configured");
        }
        // Merge the cached dates list with the dates of every memory cached.
        getMemories().mergeFrom(
                MyDeticSQLDBContract.getMemoryDates(
                        this.dbHandle, config.getUserName(), config.getActiveDataStore()));
        getMemories().mergeFrom(
                MyDeticSQLDBContract.getMemoryDetailDates(
                        this.dbHandle, config.getUserName(), config.getActiveDataStore()));
    }

    void loadMemoryDatesFromApi(final Context context,
                                       final MemoryApi.MemoryListListener externalListener) {
        getApi().getMemories(config.getUserName(), new MemoryApi.MemoryListListener() {
            @Override
            public void onApiResponse(MemoryDataList newMemories) {
                try {
                    MyDeticSQLDBContract.putMemoryDates(dbHandle,
                            config.getUserName(), config.getActiveDataStore(), newMemories);
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

    /** AsyncTask for doing things when the SQLite cache becomes available */
    private class CacheWaiterTask extends AsyncTask<Runnable, Void, Void> {
        @Override
        protected Void doInBackground(Runnable... params) {
            long startTime = SystemClock.elapsedRealtime();
            while (dbHandle == null
                    && ((SystemClock.elapsedRealtime() - startTime) < 10000L)) {
                SystemClock.sleep(250);
            }
            // Run runnables on the main thread as they likely do UI stuff.
            Handler mainHandler = new Handler(Looper.getMainLooper());
            for (Runnable runnable : params) {
                mainHandler.post(runnable);
            }
            return null;
        }
    }

    /** Run a task when the SQLite cache is ready. */
    void onCacheReady(Runnable runnable) {
        new CacheWaiterTask().execute(new Runnable[] {runnable});
    }

    /**
     * Update the API and user params from the config (eg when it has changed).
     * Clear existing memory data in RAM.
     */
    void refreshSettingsFromConfig(Context context) {
        // Init the net.ghosttrails.www.mydetic.api and memory list
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
            } catch (MyDeticException | CloneNotSupportedException e) {
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
