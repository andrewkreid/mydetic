package net.ghosttrails.www.mydetic.api;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import java.time.LocalDate;

/** Interface for classes that implement the MemoryData service to fetch and store memories. */
public interface MemoryApi {

  /**
   * @param userId which user's memories to use
   * @param listener callback to receive the memory list.
   */
  void getMemories(String userId, MemoryListListener listener);

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either date can be null, which
   * indicates no bound on the range in that direction.
   *
   * @param userId which user's memories to use
   * @param fromDate If not null, only memories from this date or later will be returned.
   * @param toDate If not null, only memories on this date and earlier will be returned.
   * @param listener callback to receive the memory list.
   */
  void getMemories(
      String userId, LocalDate fromDate, LocalDate toDate, MemoryListListener listener);

  /**
   * @param userId which user's memories to use
   * @param memoryDate the date to get the memory for.
   * @param listener callback to receive the memory.
   */
  void getMemory(String userId, LocalDate memoryDate, SingleMemoryListener listener);

  /**
   * Adds or updates a memory
   *
   * @param userId which user's memories to use
   * @param memory The memory to add/update.
   * @param listener callback to receive the memory.
   */
  void putMemory(String userId, MemoryData memory, SingleMemoryListener listener);

  /**
   * @param userId which user's memories to use
   * @param memoryDate The date of the memory to delete
   * @param listener callback to receive the memory.
   */
  void deleteMemory(String userId, LocalDate memoryDate, SingleMemoryListener listener);

  /** Callback interface for Api methods that return a MemoryDataList */
  interface MemoryListListener {
    void onApiResponse(MemoryDataList memories);

    void onApiError(MyDeticException exception);
  }

  /** Callback interface for Api methods that return a MemoryData */
  interface SingleMemoryListener {
    void onApiResponse(MemoryData memory);

    void onApiError(MyDeticException exception);
  }
}
