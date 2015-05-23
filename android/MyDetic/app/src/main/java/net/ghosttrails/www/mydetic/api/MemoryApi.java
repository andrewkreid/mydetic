package net.ghosttrails.www.mydetic.api;

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
   * @param userId
   * @return A list of all memories for user userId
   */
  MemoryDataList getMemories(String userId) throws MyDeticReadFailedException;

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either
   * date can be null, which indicates no bound on he range in that direction.
   *
   * @param userId
   * @param fromDate
   * @param toDate
   * @return
   */
  MemoryDataList getMemories(String userId, Date fromDate, Date toDate)
      throws MyDeticReadFailedException;

  /**
   * @param userId
   * @param memoryDate
   * @return a MemoryData object containing the memory for the userId and date.
   * @throws MyDeticNoMemoryFoundException if no memory exists.
   */
  MemoryData getMemory(String userId,
                       Date memoryDate) throws MyDeticReadFailedException,
      MyDeticNoMemoryFoundException;

  /**
   * Adds or updates a memory
   *
   * @param userId
   * @param memory
   * @return The added memory.
   * @throws MyDeticWriteFailedException
   */
  MemoryData putMemory(String userId, MemoryData memory) throws
      MyDeticWriteFailedException;

  /**
   * @param userId
   * @param memoryDate
   * @return true if there is a memory for the userId on memoryDate,
   * false otherwise.
   */
  boolean hasMemory(String userId, Date memoryDate) throws
      MyDeticReadFailedException;

  /**
   * @param userId
   * @param memoryDate
   * @return
   * @throws MyDeticNoMemoryFoundException
   */
  MemoryData deleteMemory(String userId,
                          Date memoryDate) throws MyDeticNoMemoryFoundException;
}
