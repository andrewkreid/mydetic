package net.ghosttrails.www.mydetic;

import android.util.Log;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import junit.framework.TestCase;

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
}
