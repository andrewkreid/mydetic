package net.ghosttrails.www.mydetic;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import net.danlew.android.joda.JodaTimeAndroid;
import net.ghosttrails.www.mydetic.cachedb.MyDeticSQLDBHelper;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Singleton to store global application state
 */
public class MyDeticApplication extends Application {

    /**
     * Name of the file used to store the application config
     */
    public static String CONFIG_FILENAME = "mydetic_config.json";

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

        JodaTimeAndroid.init(this);

        MemoryAppState appState = MemoryAppState.getInstance();

        appState.setConfig(getConfig());

        appState.setCacheDbHelper(new MyDeticSQLDBHelper(this));
        getDbHandle(appState.getCacheDbHelper());

        appState.refreshSettingsFromConfig(this);
    }

    public MyDeticConfig getConfig() {
        // Lazy initialisation
        MyDeticConfig config = new MyDeticConfig();
        try {
            config.loadFromFile(this, CONFIG_FILENAME);
        } catch (FileNotFoundException e) {
            // Not a problem, just the first time the app has been used so there's
            // no config yet.
        } catch (IOException e) {
            AppUtils.smallToast(this, "Error loading configuration");
        } catch (JSONException e) {
            AppUtils.smallToast(this, "Invalid configuration format");
        }
        return config;
    }

    /**
     * AsyncTask to load/create the SQLite cache on a subthread
     */
    class GetDBHandleTask extends AsyncTask<MyDeticSQLDBHelper, Void, SQLiteDatabase> {

        @Override
        protected void onPostExecute(SQLiteDatabase sqLiteDatabase) {
            MemoryAppState appState = MemoryAppState.getInstance();
            appState.setDbHandle(sqLiteDatabase);
        }

        @Override
        protected SQLiteDatabase doInBackground(MyDeticSQLDBHelper... myDeticSQLDBHelpers) {
            MyDeticSQLDBHelper helper = myDeticSQLDBHelpers[0];
            return helper.getWritableDatabase();
        }
    }

    /**
     * Set up database on an Async thread as recommended
     */
    private void getDbHandle(MyDeticSQLDBHelper helper) {
        GetDBHandleTask task = new GetDBHandleTask();
        task.execute(helper);
    }
}
