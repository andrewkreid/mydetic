package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Class to store the application's configuration.
 */
public class MyDeticConfig {

  // JSON config key names.
  public static final String KEY_CONFIG_VERSION = "configVersion";
  public static final String KEY_ACTIVE_DATA_STORE = "activeDataStore";
  public static final String KEY_API_URL = "apiUrl";
  public static final String KEY_USER_NAME = "userName";
  public static final String KEY_USER_PASSWORD = "userPassword";
  public static final String KEY_IS_USING_SECURITY_PIN = "isUsingSecurityPin";
  public static final String KEY_SECURITY_PIN = "securityPin";

  public static String DS_INRAM = "In RAM (testing)";
  public static String DS_RESTAPI = "REST API";

  /** key for password encryption. TODO: move this to a config file */
  private static String secretKey = "16utS1WskaJqTtSRxE6hYA==:CKw5QpwyayYsXIs0TMas2Yv2k2dXXeAgTa0ijWoQyTw=";

  /** config format version. Can be used to help handle non backwards-compatible config file format
   *  changes.
   */
  private static int CONFIG_VERSION = 1;

  private String activeDataStore;
  private String apiUrl;
  private String userName;
  private String userPassword;
  private boolean isUsingSecurityPin;
  private String securityPin;

  public MyDeticConfig() {
    activeDataStore = DS_INRAM;
    apiUrl = "";
    userName = "";
    userPassword = "";
    isUsingSecurityPin = false;
    securityPin = "1234";
  }

  /**
   * Load the config from the app's internal storage.
   *
   * @param context the application context (needed for file IO).
   * @param filename the filename to load from
   */
  public void loadFromFile(Context context, String filename) throws
      IOException, JSONException {
    FileInputStream fis = context.openFileInput(filename);
    loadFromStream(fis);
    fis.close();
  }

  public void loadFromStream(InputStream is) throws IOException, JSONException {
    InputStreamReader reader = new InputStreamReader(is);
    BufferedReader bufReader = new BufferedReader(reader);
    StringBuilder fileContents = new StringBuilder();
    String curLine;
    while ((curLine = bufReader.readLine()) != null) {
      fileContents.append(curLine);
      fileContents.append("\n");
    }
    deserializeFromJSON(fileContents.toString());
    bufReader.close();
  }

  public void saveToStream(OutputStream os) throws JSONException, IOException {
    os.write(serializeToJSON().getBytes());
  }

  /**
   * Save the config to the filename provided.
   * @param context the application context (needed for file IO).
   * @param filename the filename to save to
   * @throws JSONException, IOException
   */
  public void saveToFile(Context context, String filename)
      throws JSONException, IOException {
    FileOutputStream fos
        = context.openFileOutput(filename, Context.MODE_PRIVATE);
    saveToStream(fos);
    fos.close();
  }

  /**
   * Serialize the config to JSON that can be saved
   * @return A String containing the JSON config
   * @throws JSONException
   */
  private String serializeToJSON() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(KEY_CONFIG_VERSION, CONFIG_VERSION);
    jsonObject.put(KEY_ACTIVE_DATA_STORE, activeDataStore);
    jsonObject.put(KEY_API_URL, apiUrl);
    jsonObject.put(KEY_USER_NAME, userName);
    jsonObject.put(KEY_USER_PASSWORD, bodgyEncrypt(userPassword));
    jsonObject.put(KEY_IS_USING_SECURITY_PIN, isUsingSecurityPin);
    jsonObject.put(KEY_SECURITY_PIN, securityPin);
    return jsonObject.toString(2);
  }

  private void deserializeFromJSON(String jsonStr) throws JSONException {
    JSONObject jsonObject = new JSONObject(jsonStr);
    int configVersion = jsonObject.getInt(KEY_CONFIG_VERSION);
    if (configVersion > CONFIG_VERSION) {
      String errMsg = String.format("Config version of %d is larger than " + "expected (%d)",
          configVersion, CONFIG_VERSION);
      Log.e("MyDeticConfig", errMsg);
      throw new JSONException(errMsg);
    } else {
      activeDataStore = jsonObject.getString(KEY_ACTIVE_DATA_STORE);
      apiUrl = jsonObject.getString(KEY_API_URL);
      userName = jsonObject.getString(KEY_USER_NAME);
      userPassword = bodgyDecrypt(jsonObject.getString(KEY_USER_PASSWORD));

      if (jsonObject.has(KEY_IS_USING_SECURITY_PIN)) {
        isUsingSecurityPin = jsonObject.getBoolean(KEY_IS_USING_SECURITY_PIN);
      }
      if (jsonObject.has(KEY_SECURITY_PIN)) {
        securityPin = jsonObject.getString(KEY_SECURITY_PIN);
      }
    }
  }

  private String bodgyEncrypt(String clearText) {
    // TODO: Implement me
    return clearText;
  }

  private String bodgyDecrypt(String encryptedText) {
    // TODO: Implement me
    return encryptedText;
  }

  /**
   * @return a list of the valid values for the dataStore property
   */
  public String[] getDataStoreList() {
    return new String[]{DS_INRAM, DS_RESTAPI};
  }

  public String getActiveDataStore() {
    return activeDataStore;
  }

  public void setActiveDataStore(String activeDataStore) {
    this.activeDataStore = activeDataStore;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  public boolean isUsingSecurityPin() {
    return isUsingSecurityPin;
  }

  public void setIsUsingSecurityPin(boolean isUsingSecurityPin) {
    this.isUsingSecurityPin = isUsingSecurityPin;
  }

  public String getSecurityPin() {
    return securityPin;
  }

  public void setSecurityPin(String securityPin) {
    this.securityPin = securityPin;
  }
}
