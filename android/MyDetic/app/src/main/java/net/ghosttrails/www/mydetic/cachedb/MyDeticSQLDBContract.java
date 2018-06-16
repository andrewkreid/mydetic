package net.ghosttrails.www.mydetic.cachedb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.util.Log;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.joda.time.LocalDate;

/** DB Schema for the SQLite on-device cache database. */
public final class MyDeticSQLDBContract {

  public static final String SQL_DELETE_MEMORIES =
      "DROP TABLE IF EXISTS " + MemoryDetailsTable.TABLE_NAME;
  public static final String SQL_DELETE_MEMORY_DATES =
      "DROP TABLE IF EXISTS " + MemoryDatesTable.TABLE_NAME;
  public static final String SQL_CLEAR_MEMORY_DATES = "DELETE FROM " + MemoryDatesTable.TABLE_NAME;
  private static final String TAG = "MyDeticSQLDBContract";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INT";
  private static final String COMMA_SEP = ",";
  public static final String SQL_CREATE_MEMORIES =
      "CREATE TABLE "
          + MemoryDetailsTable.TABLE_NAME
          + " ("
          + MemoryDetailsTable._ID
          + " INTEGER PRIMARY KEY,"
          + MemoryDetailsTable.COLUMN_NAME_USER_ID
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDetailsTable.COLUMN_NAME_DATE
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDetailsTable.COLUMN_NAME_API_TYPE
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDetailsTable.COLUMN_NAME_MEMORY_TEXT
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDetailsTable.COLUMN_NAME_REVISION
          + INT_TYPE
          + COMMA_SEP
          + MemoryDetailsTable.COLUMN_NAME_STATUS
          + INT_TYPE
          + " )";
  public static final String SQL_CREATE_MEMORY_DATES =
      "CREATE TABLE "
          + MemoryDatesTable.TABLE_NAME
          + " ("
          + MemoryDatesTable.COLUMN_NAME_API_TYPE
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDatesTable.COLUMN_NAME_USER_ID
          + TEXT_TYPE
          + COMMA_SEP
          + MemoryDatesTable.COLUMN_NAME_DATE
          + TEXT_TYPE
          + " )";

  public MyDeticSQLDBContract() {}

  public static MemoryDataList getMemoryDetailDates(
      SQLiteDatabase db, String userId, String apiType) {

    MemoryDataList memoryDataList = new MemoryDataList(userId);
    String[] projection = {MemoryDetailsTable.COLUMN_NAME_DATE};

    String sortOrder = MemoryDetailsTable.COLUMN_NAME_DATE + " ASC";

    String selectClause =
        String.format(
            "%s = ? AND %s = ?",
            MemoryDetailsTable.COLUMN_NAME_USER_ID, MemoryDetailsTable.COLUMN_NAME_API_TYPE);
    String selectionArgs[] = {userId, apiType};

    try (Cursor cursor =
        db.query(
            MemoryDetailsTable.TABLE_NAME, // The table to query
            projection, // The columns to return
            selectClause, // The columns for the WHERE clause
            selectionArgs, // The values for the WHERE clause
            null, // don't group the rows
            null, // don't filter by row groups
            sortOrder // The sort order
            )) {
      while (cursor.moveToNext()) {
        String memoryDate =
            cursor.getString(cursor.getColumnIndexOrThrow(MemoryDetailsTable.COLUMN_NAME_DATE));
        memoryDataList.setDate(Utils.parseIsoDate(memoryDate));
      }
    }
    return memoryDataList;
  }

  public static MemoryDataList getMemoryDates(SQLiteDatabase db, String userId, String apiType) {
    MemoryDataList memoryDataList = new MemoryDataList(userId);
    String[] projection = {MemoryDatesTable.COLUMN_NAME_DATE};

    String sortOrder = MemoryDatesTable.COLUMN_NAME_DATE + " ASC";

    String selectClause =
        String.format(
            "%s = ? AND %s = ?",
            MemoryDatesTable.COLUMN_NAME_USER_ID, MemoryDatesTable.COLUMN_NAME_API_TYPE);
    String selectionArgs[] = {userId, apiType};

    try {
      try (Cursor cursor =
          db.query(
              MemoryDatesTable.TABLE_NAME, // The table to query
              projection, // The columns to return
              selectClause, // The columns for the WHERE clause
              selectionArgs, // The values for the WHERE clause
              null, // don't group the rows
              null, // don't filter by row groups
              sortOrder // The sort order
              )) {
        while (cursor.moveToNext()) {
          String memoryDate =
              cursor.getString(cursor.getColumnIndexOrThrow(MemoryDatesTable.COLUMN_NAME_DATE));
          memoryDataList.setDate(Utils.parseIsoDate(memoryDate));
        }
      }
    } catch (SQLiteException e) {
      Log.e(TAG, e.getMessage());
    }
    return memoryDataList;
  }

  /** Cache a MemoryDataList */
  public static void putMemoryDates(
      SQLiteDatabase db, String userId, String apiType, MemoryDataList memoryDataList) {
    db.execSQL(SQL_CLEAR_MEMORY_DATES);
    db.beginTransaction();
    for (LocalDate date : memoryDataList) {
      putMemoryDate(db, userId, apiType, date);
    }
    db.setTransactionSuccessful();
    db.endTransaction();
  }

  public static void putMemoryDate(
      SQLiteDatabase db, String userId, String apiType, LocalDate date) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(MemoryDatesTable.COLUMN_NAME_USER_ID, userId);
    contentValues.put(MemoryDatesTable.COLUMN_NAME_DATE, Utils.isoFormat(date));
    contentValues.put(MemoryDatesTable.COLUMN_NAME_API_TYPE, apiType);
    db.insert(MemoryDatesTable.TABLE_NAME, null, contentValues);
  }

  public static MemoryData getMemory(
      SQLiteDatabase db, String userId, String apiType, LocalDate memoryDate) {
    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    String[] projection = {
      MemoryDetailsTable._ID,
      MemoryDetailsTable.COLUMN_NAME_USER_ID,
      MemoryDetailsTable.COLUMN_NAME_DATE,
      MemoryDetailsTable.COLUMN_NAME_API_TYPE,
      MemoryDetailsTable.COLUMN_NAME_MEMORY_TEXT,
      MemoryDetailsTable.COLUMN_NAME_REVISION,
      MemoryDetailsTable.COLUMN_NAME_STATUS
    };

    // How you want the results sorted in the resulting Cursor
    String sortOrder = MemoryDetailsTable._ID + " ASC";

    String selectionArgs[] = {userId, Utils.isoFormat(memoryDate), apiType};

    Cursor cursor =
        db.query(
            MemoryDetailsTable.TABLE_NAME, // The table to query
            projection, // The columns to return
            getSelectClause(), // The columns for the WHERE clause
            selectionArgs, // The values for the WHERE clause
            null, // don't group the rows
            null, // don't filter by row groups
            sortOrder // The sort order
            );

    MemoryData retval = null;
    if (cursor.moveToFirst()) {
      retval = new MemoryData();
      retval.setUserId(
          cursor.getString(cursor.getColumnIndexOrThrow(MemoryDetailsTable.COLUMN_NAME_USER_ID)));
      retval.setMemoryText(
          cursor.getString(
              cursor.getColumnIndexOrThrow(MemoryDetailsTable.COLUMN_NAME_MEMORY_TEXT)));
      retval.setRevision(
          cursor.getInt(cursor.getColumnIndexOrThrow(MemoryDetailsTable.COLUMN_NAME_REVISION)));
      // TODO: Set Status
      try {
        retval.setMemoryDate(
            Utils.parseIsoDate(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(MemoryDetailsTable.COLUMN_NAME_DATE))));
      } catch (IllegalArgumentException e) {
        // cache entry saved with bad date.
        return null;
      }
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return retval;
  }

  public static void addMemory(SQLiteDatabase db, String apiType, MemoryData memory)
      throws MyDeticException {
    ContentValues values = buildContentValues(apiType, memory);

    // Insert the new row, returning the primary key value of the new row
    long newRowId;
    newRowId = db.insert(MemoryDetailsTable.TABLE_NAME, null, values);
    if (newRowId == -1) {
      throw new MyDeticException(
          String.format(
              "Failed to insert CacheDB record for %s:%s:%s",
              memory.getUserId(), Utils.isoFormat(memory.getMemoryDate()), apiType));
    }
  }

  public static void updateMemory(SQLiteDatabase db, String apiType, MemoryData memory)
      throws MyDeticException {
    // New value for one column
    ContentValues values = buildContentValues(apiType, memory);

    String selectionArgs[] = {memory.getUserId(), Utils.isoFormat(memory.getMemoryDate()), apiType};

    int count = db.update(MemoryDetailsTable.TABLE_NAME, values, getSelectClause(), selectionArgs);
    if (count != 1) {
      throw new MyDeticException(
          String.format(
              "Failed to update CacheDB record for %s:%s:%s",
              memory.getUserId(), Utils.isoFormat(memory.getMemoryDate()), apiType));
    }
  }

  /** Add or update a memory */
  public static void putMemory(SQLiteDatabase db, String apiType, MemoryData memory)
      throws MyDeticException {
    if (getMemory(db, memory.getUserId(), apiType, memory.getMemoryDate()) == null) {
      addMemory(db, apiType, memory);
    } else {
      updateMemory(db, apiType, memory);
    }
  }

  /**
   * Delete all memories in the database.
   *
   * @param db a writable database handle.
   */
  public static void deleteMemories(SQLiteDatabase db) {
    db.execSQL(MyDeticSQLDBContract.SQL_DELETE_MEMORIES);
    db.execSQL(MyDeticSQLDBContract.SQL_CREATE_MEMORIES);
    db.execSQL(MyDeticSQLDBContract.SQL_DELETE_MEMORY_DATES);
    db.execSQL(MyDeticSQLDBContract.SQL_CREATE_MEMORY_DATES);
  }

  private static String getSelectClause() {
    return String.format(
        "%s = ? AND %s = ? AND %s = ?",
        MemoryDetailsTable.COLUMN_NAME_USER_ID,
        MemoryDetailsTable.COLUMN_NAME_DATE,
        MemoryDetailsTable.COLUMN_NAME_API_TYPE);
  }

  /** Build a ContentValues hash for an SQL insert or update */
  private static ContentValues buildContentValues(String apiType, MemoryData memory) {
    ContentValues values = new ContentValues();
    values.put(MemoryDetailsTable.COLUMN_NAME_USER_ID, memory.getUserId());
    values.put(MemoryDetailsTable.COLUMN_NAME_DATE, Utils.isoFormat(memory.getMemoryDate()));
    values.put(MemoryDetailsTable.COLUMN_NAME_API_TYPE, apiType);
    values.put(MemoryDetailsTable.COLUMN_NAME_MEMORY_TEXT, memory.getMemoryText());
    values.put(MemoryDetailsTable.COLUMN_NAME_REVISION, memory.getRevision());
    values.put(MemoryDetailsTable.COLUMN_NAME_STATUS, memory.getCacheState());
    return values;
  }

  /* Table that just stores the list of dates that contain memories */
  public abstract static class MemoryDatesTable implements BaseColumns {
    public static final String TABLE_NAME = "memorydates";
    public static final String COLUMN_NAME_USER_ID = "userId";
    public static final String COLUMN_NAME_DATE = "memoryDate";
    // Type of API (eg MyDeticConfig.DS_INRAM). So in ram API entries don't interfere with
    // real ones.
    public static final String COLUMN_NAME_API_TYPE = "apiType";
  }

  /* Inner class that defines the table contents */
  public abstract static class MemoryDetailsTable implements BaseColumns {
    public static final String TABLE_NAME = "memories";
    public static final String COLUMN_NAME_USER_ID = "userId";
    public static final String COLUMN_NAME_DATE = "memoryDate";

    // Type of API (eg MyDeticConfig.DS_INRAM). So in ram API entries don't interfere with
    // real ones.
    public static final String COLUMN_NAME_API_TYPE = "apiType";
    public static final String COLUMN_NAME_MEMORY_TEXT = "memoryText";
    public static final String COLUMN_NAME_REVISION = "revision";

    // Whether the entry is pending upload or not.
    public static final String COLUMN_NAME_STATUS = "status";
  }
}
