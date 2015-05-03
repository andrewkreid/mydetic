package net.ghosttrails.www.mydetic.api;

import java.util.Date;

/**
 * Interface for classes that implement the MemoryData service to
 * fetch and store memories.
 */
public interface MemoryApi {

    /**
     *
     * @param userId
     * @return A list of all memories for user userId
     */
    MemoryDataList getMemories(String userId);

    /**
     * Get a list of memories between fromDate and toDate (inclusive). Either
     * date can be null, which indicates no bound on he range in that direction.
     *
     * @param userId
     * @param fromDate
     * @param toDate
     * @return
     */
    MemoryDataList getMemories(String userId, Date fromDate, Date toDate);

    /**
     *
     * @param userId
     * @param memoryDate
     * @return a MemoryData object containing the memory for the userId and date.
     * @throws NoMemoryFoundException if no memory exists.
     */
    MemoryData getMemory(String userId, Date memoryDate) throws NoMemoryFoundException;

    /**
     * Adds or updates a memory
     * @param userId
     * @param memory
     * @return The added memory.
     */
    MemoryData putMemory(String userId, MemoryData memory);

    /**
     *
     * @param userId
     * @param memoryDate
     * @return true if there is a memory for the userId on memoryDate,
     *         false otherwise.
     */
    boolean hasMemory(String userId, Date memoryDate);

    /**
     *
     * @param userId
     * @param memoryDate
     * @return
     * @throws NoMemoryFoundException
     */
    MemoryData deleteMemory(String userId, Date memoryDate) throws NoMemoryFoundException;
}
