package net.ghosttrails.www.mydetic.api;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of Volley's JsonObjectRequest that adds an HTTP Basic Auth header.
 * <p/>
 * Sourced from (https://yakivmospan.wordpress.com/2014/04/04/volley-authorization/)
 */
public class BasicAuthJsonObjectRequest extends JsonObjectRequest {

  private String username;
  private String password;

  public BasicAuthJsonObjectRequest(String username, String password, int method, String url,
                                    JSONObject jsonRequest,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
    super(method, url, jsonRequest, listener, errorListener);
    this.username = username;
    this.password = password;
  }

  public BasicAuthJsonObjectRequest(String username, String password, String url,
                                    JSONObject jsonRequest,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
    super(url, jsonRequest, listener, errorListener);
    this.username = username;
    this.password = password;
  }

  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    return createBasicAuthHeader("user", "password");
  }

  Map<String, String> createBasicAuthHeader(String username, String password) {
    Map<String, String> headerMap = new HashMap<String, String>();

    String credentials = username + ":" + password;
    String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    headerMap.put("Authorization", "Basic " + encodedCredentials);

    return headerMap;
  }
}
