package net.ghosttrails.www.mydetic.cachedb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;

import java.text.ParseException;
import java.util.Date;

/**
 * DB Schema for the SQLite on-device cache database.
 */
public final class MyDeticSQLDBContract {

  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INT";
  private static final String COMMA_SEP = ",";
  public static final String SQL_CREATE_MEMORIES =
      "CREATE TABLE " + MemoryTable.TABLE_NAME + " (" +
          MemoryTable._ID + " INTEGER PRIMARY KEY," +
          MemoryTable.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
          MemoryTable.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
          MemoryTable.COLUMN_NAME_API_TYPE + TEXT_TYPE + COMMA_SEP +
          MemoryTable.COLUMN_NAME_MEMORY_TEXT + TEXT_TYPE + COMMA_SEP +
          MemoryTable.COLUMN_NAME_REVISION + INT_TYPE + COMMA_SEP +
          MemoryTable.COLUMN_NAME_STATUS + INT_TYPE +
          " )";

  public static final String SQL_DELETE_MEMORIES =
      "DROP TABLE IF EXISTS " + MemoryTable.TABLE_NAME;

  public MyDeticSQLDBContract() {}

  /* Inner class that defines the table contents */
  public static abstract class MemoryTable implements BaseColumns {
    public static final String TABLE_NAME = "memories";
    public static final String COLUMN_NAME_USER_ID = "userId";
    public static final String COLUMN_NAME_DATE = "memoryDate";

    // Type of API (eg MyDeticConfig.DS_INRAM). So in ram API entries don't interfere with
    // real ones.
    public static final String COLUMN_NAME_API_TYPE = "apiType";
    public static final String COLUMN_NAME_MEMORY_TEXT = "memoryText";
    public static final String COLUMN_NAME_REVISION = "revision";

    // Whether the entry is pending upload or not (TODO).
    public static final String COLUMN_NAME_STATUS = "status";
  }

  public static MemoryData getMemory(SQLiteDatabase db, String userId,
                                     String apiType, Date memoryDate) {
    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    String[] projection = {
        MemoryTable._ID,
        MemoryTable.COLUMN_NAME_USER_ID,
        MemoryTable.COLUMN_NAME_DATE,
        MemoryTable.COLUMN_NAME_API_TYPE,
        MemoryTable.COLUMN_NAME_MEMORY_TEXT,
        MemoryTable.COLUMN_NAME_REVISION,
        MemoryTable.COLUMN_NAME_STATUS
    };

    // How you want the results sorted in the resulting Cursor
    String sortOrder = MemoryTable._ID + " ASC";

    String selection = String.format("%s = ? AND %s = ? AND %s = ?",
        MemoryTable.COLUMN_NAME_USER_ID,
        MemoryTable.COLUMN_NAME_DATE,
        MemoryTable.COLUMN_NAME_API_TYPE);

    String selectionArgs[] = {userId, Utils.isoFormat(memoryDate), apiType};

    Cursor cursor = db.query(
        MemoryTable.TABLE_NAME,  // The table to query
        projection,                               // The columns to return
        selection,                                // The columns for the WHERE clause
        selectionArgs,                            // The values for the WHERE clause
        null,                                     // don't group the rows
        null,                                     // don't filter by row groups
        sortOrder                                 // The sort order
    );

    MemoryData retval = null;
    if (cursor.moveToFirst()) {
      retval = new MemoryData();
      retval.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(MemoryTable.COLUMN_NAME_USER_ID)));
      try {
        retval.setMemoryDate(
            Utils.parseIsoDate(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(MemoryTable.COLUMN_NAME_DATE))));
      } catch (ParseException e) {
        // cache entry saved with bad date.
        return null;
      }
    }
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
    return retval;
  }

  public static void putMemory(SQLiteDatabase db, MemoryData memory) {
    // TODO:
  }
}
