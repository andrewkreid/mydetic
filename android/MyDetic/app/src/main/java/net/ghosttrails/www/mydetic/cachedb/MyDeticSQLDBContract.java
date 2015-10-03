package net.ghosttrails.www.mydetic.cachedb;

import android.provider.BaseColumns;

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
    public static final String COLUMN_NAME_API_TYPE = "apiIdx";
    public static final String COLUMN_NAME_MEMORY_TEXT = "memoryText";
    public static final String COLUMN_NAME_REVISION = "revision";

    // Whether the entry is pending upload or not (TODO).
    public static final String COLUMN_NAME_STATUS = "status";
  }

}
