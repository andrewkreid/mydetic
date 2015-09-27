package net.ghosttrails.www.mydetic.api;

import com.android.volley.VolleyError;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.util.Date;

/**
 * Interface for classes that implement the MemoryData service to
 * fetch and store memories.
 */
public interface MemoryApi {

  /**
   * Callback interface for Api methods that return a MemoryDataList
   */
  interface MemoryListListener {
    void onApiResponse(MemoryDataList memories);

    void onApiError(MyDeticException exception);
  }

  /**
   * Callback interface for Api methods that return a MemoryData
   */
  interface SingleMemoryListener {
    void onApiResponse(MemoryData memory);

    void onApiError(MyDeticException exception);
  }

  /**
   * @param userId   which user's memories to use
   * @param listener callback to receive the memory list.
   */
  void getMemories(String userId, MemoryListListener listener);

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either
   * date can be null, which indicates no bound on the range in that direction.
   *
   * @param userId   which user's memories to use
   * @param fromDate If not null, only memories from this date or later will be
   *                 returned.
   * @param toDate   If not null, only memories on this date and earlier will be
   *                 returned.
   * @param listener callback to receive the memory list.
   */
  void getMemories(String userId, Date fromDate, Date toDate,
                   MemoryListListener listener);

  /**
   * @param userId     which user's memories to use
   * @param memoryDate the date to get the memory for.
   * @param listener   callback to receive the memory.
   */
  void getMemory(String userId, Date memoryDate, SingleMemoryListener
      listener);

  /**
   * Adds or updates a memory
   *
   * @param userId   which user's memories to use
   * @param memory   The memory to add/update.
   * @param listener callback to receive the memory.
   */
  void putMemory(String userId, MemoryData memory, SingleMemoryListener
      listener);

  /**
   * @param userId     which user's memories to use
   * @param memoryDate The date of the memory to delete
   * @param listener   callback to receive the memory.
   */
  void deleteMemory(String userId, Date memoryDate, SingleMemoryListener
      listener);
}
