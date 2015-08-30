package net.ghosttrails.www.mydetic.api;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

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

    // Append API_PATH unless the user seems to have specified something themselves.
    if ((configUri.getPath() == null) || (configUri.getPath().length() <= 1)) {
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
    getMemories(userId, null, null, listener);
  }

  @Override
  public void getMemories(String userId, Date fromDate, Date toDate,
                          final MemoryListListener listener) {
    String url = String.format("%s/memories?user_id=%s", getApiUrl(), userId);
    if (fromDate != null) {
      url = String.format("%s&start_date=%s", url, Utils.isoFormat(fromDate));
    }
    if (toDate != null) {
      url = String.format("%s&end_date=%s", url, Utils.isoFormat(toDate));
    }
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
        listener.onApiError(new MyDeticException(formatVolleyError(error), error));
      }
    });
    requestQueue.add(jsObjRequest);
  }

  @Override
  public void getMemory(String userId, Date memoryDate, final SingleMemoryListener listener) {
    String url = String.format("%s/memories/%s?user_id=%s", getApiUrl(),
        Utils.isoFormat(memoryDate), userId);
    BasicAuthJsonObjectRequest jsObjRequest = new BasicAuthJsonObjectRequest(config.getUserName(),
        config.getUserPassword(),
        Request.Method.GET, url, null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              listener.onApiResponse(MemoryData.fromJSON(response));
            } catch (MyDeticException e) {
              listener.onApiError(e);
            }
          }
        }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        listener.onApiError(new MyDeticException(formatVolleyError(error), error));
      }
    });
    requestQueue.add(jsObjRequest);
  }

  @Override
  public void putMemory(final String userId, final MemoryData memory, final SingleMemoryListener listener) {
    String url = String.format("%s/memories/%s?user_id=%s", getApiUrl(),
        Utils.isoFormat(memory.getMemoryDate()), userId);
    BasicAuthJsonObjectRequest jsObjRequest = null;
    try {
      jsObjRequest = new BasicAuthJsonObjectRequest(config.getUserName(),
          config.getUserPassword(),
          Request.Method.PUT, url, memory.toJSON(),
          new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
              try {
                listener.onApiResponse(MemoryData.fromJSON(response));
              } catch (MyDeticException e) {
                listener.onApiError(e);
              }
            }
          }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
          if (error.networkResponse != null) {
            if (error.networkResponse.statusCode == 404) {
              // TODO: If the response to the PUT was 404, then try a POST to create a new memory.
              createMemory(userId, memory, listener);
            } else {
              listener.onApiError(new MyDeticException(String.format("Got %d when saving Memory",
                  error.networkResponse.statusCode), error));
            }
          } else {
            listener.onApiError(new MyDeticException(formatVolleyError(error), error));
          }
        }
      });
    } catch (MyDeticException e) {
      // Error before making REST call.
      listener.onApiError(e);
    }
    requestQueue.add(jsObjRequest);
  }

  @Override
  public void deleteMemory(String userId, Date memoryDate, final SingleMemoryListener listener) {
    listener.onApiError(new MyDeticException("Not Implemented"));
  }

  private void createMemory(final String userId, final MemoryData memory, final SingleMemoryListener listener) {

    String url = String.format("%s/memories?user_id=%s", getApiUrl(), userId);
    BasicAuthJsonObjectRequest jsObjRequest = null;
    try {
      jsObjRequest = new BasicAuthJsonObjectRequest(config.getUserName(),
          config.getUserPassword(),
          Request.Method.POST, url, memory.toJSON(),
          new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
              try {
                listener.onApiResponse(MemoryData.fromJSON(response));
              } catch (MyDeticException e) {
                listener.onApiError(e);
              }
            }
          }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
          listener.onApiError(new MyDeticException(formatVolleyError(error), error));
        }
      });
    } catch (MyDeticException e) {
      // Error before making REST call.
      listener.onApiError(e);
    }
    requestQueue.add(jsObjRequest);
  }

  private String formatVolleyError(VolleyError v) {
    if (v.networkResponse != null) {
      return String.format("Network Error: %d", v.networkResponse.statusCode);
    } else {
      return "Network Error:<unknown>";
    }
  }
}
