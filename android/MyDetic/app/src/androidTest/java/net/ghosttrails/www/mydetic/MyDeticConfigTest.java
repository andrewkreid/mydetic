package net.ghosttrails.www.mydetic;

import android.util.Log;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import junit.framework.TestCase;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

/**
 * Tests for MyDeticConfig
 */
public class MyDeticConfigTest extends TestCase {

  public void testKeysCanBeGenerated() throws GeneralSecurityException {
    AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKey();
    String strKey = keys.toString();
    Log.i("MyDetic tests", strKey);
    assertNotNull(strKey);
    assertTrue(strKey.length() > 0);
  }

  public void testSerialization() throws IOException, JSONException {
    MyDeticConfig config = new MyDeticConfig();
    config.setActiveDataStore(MyDeticConfig.DS_RESTAPI);
    config.setApiUrl("https://www.mydetic.net/");
    config.setUserName("mreynolds");
    config.setUserPassword("password123");

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    config.saveToStream(os);

    String jsonConfigStr = new String(os.toByteArray());
    assertTrue(jsonConfigStr.contains("mreynolds"));

    MyDeticConfig reloadedConfig = new MyDeticConfig();
    reloadedConfig.loadFromStream(new ByteArrayInputStream(os.toByteArray()));

    assertEquals(MyDeticConfig.DS_RESTAPI, reloadedConfig.getActiveDataStore());
    assertEquals("https://www.mydetic.net/", reloadedConfig.getApiUrl());
    assertEquals("mreynolds", reloadedConfig.getUserName());
    assertEquals("password123", reloadedConfig.getUserPassword());
  }
}
