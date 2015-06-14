package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class to store the application's configuration.
 */
public class MyDeticConfig {

  public static String DS_INRAM = "In RAM (testing)";
  public static String DS_RESTAPI = "REST API";

  /** config format version. Can be used to help handle non backwards-compatible
   *  config file format changes.
   */
  private static int CONFIG_VERSION = 1;

  private String activeDataStore;
  private String apiUrl;
  private String userName;
  private String userPassword;

  public MyDeticConfig() {
    activeDataStore = DS_INRAM;
    apiUrl = "";
    userName = "";
    userPassword = "";
  }

  /**
   * Load the config from the app's internal storage.
   *
   * @param context the application context (needed for file IO).
   * @param filename the filename to load from
   */
  public void load(Context context, String filename) throws
      IOException, JSONException {
    FileInputStream fis = context.openFileInput(filename);
    InputStreamReader reader = new InputStreamReader(fis);
    BufferedReader bufReader = new BufferedReader(reader);
    StringBuilder fileContents = new StringBuilder();
    String curLine = bufReader.readLine();
    while (curLine != null) {
      fileContents.append(curLine);
      fileContents.append("\n");
    }
    JSONObject jsonObject = new JSONObject(fileContents.toString());
    int configVersion = jsonObject.getInt("configVersion");
    if (configVersion > CONFIG_VERSION) {
      String errMsg = String.format("Config version of %d is larger than " +
              "expected (%d)",
          configVersion, CONFIG_VERSION);
      Log.e("MyDeticConfig", errMsg);
      throw new JSONException(errMsg);
    } else {
      activeDataStore = jsonObject.getString("activeDataStore");
      apiUrl = jsonObject.getString("apiUrl");
      userName = jsonObject.getString("userName");
      userPassword = bodgyDecrypt(jsonObject.getString("userPassword"));
    }
  }

  /**
   * Save the config to the filename provided.
   * @param context the application context (needed for file IO).
   * @param filename the filename to save to
   * @throws JSONException
   */
  public void save(Context context, String filename)
      throws JSONException, IOException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("configVersion", CONFIG_VERSION);
    jsonObject.put("activeDataSource", activeDataStore);
    jsonObject.put("apiUrl", apiUrl);
    jsonObject.put("userName", userName);
    jsonObject.put("userPassword", bodgyEncrypt(userPassword));

    FileOutputStream fos
        = context.openFileOutput(filename, Context.MODE_PRIVATE);
    fos.write(jsonObject.toString(4).getBytes());
    fos.close();
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
}
