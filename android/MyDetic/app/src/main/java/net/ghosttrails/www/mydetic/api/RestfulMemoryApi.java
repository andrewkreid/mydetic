package net.ghosttrails.www.mydetic.api;

import android.content.Context;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.ghosttrails.www.mydetic.MyDeticConfig;
import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticReadFailedException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.util.Date;

/**
 * A MemoryApi implementation that calls the REST API over HTTP(S)
 */
public class RestfulMemoryApi implements MemoryApi {

  // The default URL path for API calls.
  private static final String API_PATH = "mydetic/api/v1.0/memories";

  private RequestQueue requestQueue;
  private Context context;
  private MyDeticConfig config;

  public RestfulMemoryApi(Context ctx, MyDeticConfig config) {
    context = ctx;
    requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    this.config = config;
  }

  /**
   * Builds the full URL for a REST request, combining the config URL and
   * possibly API_URL (if the config is only a host name).
   * @return The full URL string.
   */
  private String getApiUrl() {
    StringBuilder builder = new StringBuilder();
    builder.append(config.getApiUrl());
    Uri configUri = Uri.parse(config.getApiUrl());
    if ((configUri.getPath() == null) || (configUri.getPath().length() == 1)) {
      // There is no path, or the path is just "/", use the default API_PATH
      if (!config.getApiUrl().endsWith("/")) {
        builder.append("/");
      }
      builder.append(API_PATH);
    }
    return builder.toString();
  }

  /**
   * @param userId which user's memories to use
   * @return A list of all memories for user userId
   */
  @Override
  public MemoryDataList getMemories(String userId) throws MyDeticReadFailedException {
    return getMemories(userId, null, null);
  }

  /**
   * Get a list of memories between fromDate and toDate (inclusive). Either
   * date can be null, which indicates no bound on he range in that direction.
   *
   * @param userId   which user's memories to use
   * @param fromDate If not null, only memories from this date or later will be
   *                 returned.
   * @param toDate   If not null, only memories on this date and earlier will be
   *                 returned.
   * @return a MemoryDataList containing the dates that have memories for the
   * userId
   */
  @Override
  public MemoryDataList getMemories(String userId, Date fromDate, Date toDate) throws MyDeticReadFailedException {
    return null;
  }

  /**
   * @param userId     which user's memories to use
   * @param memoryDate the date to get the memory for.
   * @return a MemoryData object containing the memory for the userId and date.
   * @throws MyDeticNoMemoryFoundException if no memory exists.
   */
  @Override
  public MemoryData getMemory(String userId, Date memoryDate) throws MyDeticReadFailedException, MyDeticNoMemoryFoundException {
    return null;
  }

  /**
   * Adds or updates a memory
   *
   * @param userId which user's memories to use
   * @param memory The memory to add/update.
   * @return The added memory.
   * @throws MyDeticWriteFailedException
   */
  @Override
  public MemoryData putMemory(String userId, MemoryData memory) throws MyDeticWriteFailedException {
    return null;
  }

  /**
   * @param userId     which user's memories to use
   * @param memoryDate The date to check for
   * @return true if there is a memory for the userId on memoryDate,
   * false otherwise.
   * @throws MyDeticReadFailedException
   */
  @Override
  public boolean hasMemory(String userId, Date memoryDate) throws MyDeticReadFailedException {
    return false;
  }

  /**
   * @param userId     which user's memories to use
   * @param memoryDate The date of the memory to delete
   * @return The deleted memory
   * @throws MyDeticNoMemoryFoundException
   */
  @Override
  public MemoryData deleteMemory(String userId, Date memoryDate) throws MyDeticNoMemoryFoundException {
    return null;
  }
}
