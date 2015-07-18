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

  @Override
  public void getMemories(String userId,
                          MemoryListListener listener) {

  }

  @Override
  public void getMemories(String userId, Date fromDate, Date toDate,
                          MemoryListListener listener) {

  }

  @Override
  public void getMemory(String userId, Date memoryDate,
                        SingleMemoryListener listener) {

  }

  @Override
  public void putMemory(String userId, MemoryData memory,
                        SingleMemoryListener listener) {

  }

  @Override
  public void deleteMemory(String userId, Date memoryDate,
                           SingleMemoryListener listener) {

  }
}
