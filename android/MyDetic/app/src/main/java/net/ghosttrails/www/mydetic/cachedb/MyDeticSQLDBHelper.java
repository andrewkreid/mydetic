package net.ghosttrails.www.mydetic.cachedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** Helper to manage the SQLite cache database. */
public class MyDeticSQLDBHelper extends SQLiteOpenHelper {

  public static final int DATABASE_VERSION = 2;
  public static final String DATABASE_NAME = "MyDetic.db";
  private static final String TAG = "MyDeticSQLDBHelper";

  public MyDeticSQLDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, String.format("SQL onCreate [%s]", MyDeticSQLDBContract.SQL_CREATE_MEMORIES));
    Log.i(TAG, String.format("SQL onCreate [%s]", MyDeticSQLDBContract.SQL_CREATE_MEMORY_DATES));
    db.execSQL(MyDeticSQLDBContract.SQL_CREATE_MEMORIES);
    db.execSQL(MyDeticSQLDBContract.SQL_CREATE_MEMORY_DATES);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    Log.i(TAG, String.format("SQL onUpgrade [%s]", MyDeticSQLDBContract.SQL_DELETE_MEMORIES));
    Log.i(TAG, String.format("SQL onUpgrade [%s]", MyDeticSQLDBContract.SQL_DELETE_MEMORY_DATES));
    db.execSQL(MyDeticSQLDBContract.SQL_DELETE_MEMORIES);
    db.execSQL(MyDeticSQLDBContract.SQL_DELETE_MEMORY_DATES);
    onCreate(db);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }
}
