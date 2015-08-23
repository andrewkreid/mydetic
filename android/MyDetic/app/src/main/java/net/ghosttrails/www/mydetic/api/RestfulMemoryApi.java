package net.ghosttrails.www.mydetic.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.ghosttrails.www.mydetic.MyDeticConfig;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.json.JSONObject;

import java.util.Date;

/**
 * A MemoryApi implementation that calls the REST API over HTTP(S)
 */
public class RestfulMemoryApi implements MemoryApi {

  // The default URL path for API calls.
  private static final String API_PATH = "mydetic/api/v1.0";

  private RequestQueue requestQueue;
  private MyDeticConfig config;

  public RestfulMemoryApi(Context ctx, MyDeticConfig config) {
    requestQueue = RequestQueueSingleton.getInstance(ctx).getRequestQueue();
    this.config = config;
  }

  /**
   * Builds the full URL for a REST request, combining the config URL and
   * possibly API_URL (if the config is only a host name).
   *
   * @return The full URL string.
   */
  private String getApiUrl() {
    StringBuilder builder = new StringBuilder();
    builder.append(config.getApiUrl());
    Uri configUri = Uri.parse(config.getApiUrl());
    Log.i("MyDetic", String.format("getPath() = %s", configUri.getPath()));
    if ((configUri.getPath() == null) || (configUri.getPath().length() <= 1)) {
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
                          final MemoryListListener listener) {
    String url = String.format("%s/memories?user_id=%s", getApiUrl(), userId);
    BasicAuthJsonObjectRequest jsObjRequest = new BasicAuthJsonObjectRequest(config.getUserName(),
        config.getUserPassword(),
        Request.Method.GET, url, null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              listener.onApiResponse(MemoryDataList.fromJSON(response));
            } catch (MyDeticException e) {
              listener.onApiError(e);
            }
          }
        }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        listener.onApiError(new MyDeticException("Network Error: " + error.getMessage(), error));
      }
    });
    requestQueue.add(jsObjRequest);
  }

  @Override
  public void getMemories(String userId, Date fromDate, Date toDate,
                          MemoryListListener listener) {
    listener.onApiError(new MyDeticException("Not Implemented"));
  }

  @Override
  public void getMemory(String userId, Date memoryDate,
                        SingleMemoryListener listener) {
    listener.onApiError(new MyDeticException("Not Implemented"));
  }

  @Override
  public void putMemory(String userId, MemoryData memory,
                        SingleMemoryListener listener) {
    listener.onApiError(new MyDeticException("Not Implemented"));
  }

  @Override
  public void deleteMemory(String userId, Date memoryDate,
                           SingleMemoryListener listener) {
    listener.onApiError(new MyDeticException("Not Implemented"));
  }
}
