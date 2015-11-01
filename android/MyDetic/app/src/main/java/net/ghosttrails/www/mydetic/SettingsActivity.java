package net.ghosttrails.www.mydetic;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;

import java.io.IOException;


public class SettingsActivity extends LockableActivity {

  private MyDeticConfig config;
  private Spinner dataSourceSpinner;
  private ArrayAdapter<String> dataSourceSpinnerAdapter;
  private EditText apiUrlEditText;
  private EditText usernameEditText;
  private EditText passwordEditText;
  private CheckBox pinEnabledCheckBox;

  /**
   * Focus loss listener that saves the config
   */
  private class PersistOnFocusLossListener implements View
      .OnFocusChangeListener {

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
      if (!hasFocus) {
        setConfigFromUI();
        saveConfig();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    MyDeticApplication app = (MyDeticApplication) getApplication();

    dataSourceSpinner = (Spinner) findViewById(R.id.dataStoreSpinner);
    apiUrlEditText = (EditText) findViewById(R.id.apiUrlEditText);
    usernameEditText = (EditText) findViewById(R.id.apiUserNameEditText);
    passwordEditText = (EditText) findViewById(R.id.passwordEditText);
    pinEnabledCheckBox = (CheckBox) findViewById(R.id.enablePinLock);

    loadConfig();

    PersistOnFocusLossListener focusLossListener = new
        PersistOnFocusLossListener();
    apiUrlEditText.setOnFocusChangeListener(focusLossListener);
    usernameEditText.setOnFocusChangeListener(focusLossListener);
    passwordEditText.setOnFocusChangeListener(focusLossListener);
    pinEnabledCheckBox.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setConfigFromUI();
        saveConfig();
      }
    });

    dataSourceSpinnerAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
            config.getDataStoreList());
    dataSourceSpinnerAdapter.setDropDownViewResource(android.R.layout
        .simple_list_item_1);
    dataSourceSpinner.setAdapter(dataSourceSpinnerAdapter);

    setUIFromConfig();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_settings, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Tap handler for the "Clear Cache" button. Empties the SQLite cache DB
   */
  public void clearCacheClicked(View view) {
    MemoryAppState.getInstance().clearMemoryCache();
    AppUtils.smallToast(getApplicationContext(), "Cache cleared.");
  }

  /**
   * Set the UI fields from the config object
   */
  private void loadConfig() {
    this.config = MemoryAppState.getInstance().getConfig();
  }

  private void saveConfig() {
    if (config != null) {
      try {
        config.saveToFile(getApplicationContext(), MyDeticApplication.CONFIG_FILENAME);
      } catch (IOException e) {
        AppUtils.smallToast(getApplicationContext(), "Error loading configuration");
      } catch (JSONException e) {
        AppUtils.smallToast(getApplicationContext(), "Invalid configuration format");
      }
    }
  }

  private void setConfigFromUI() {
    if(config != null) {
      config.setApiUrl(apiUrlEditText.getText().toString());
      config.setUserName(usernameEditText.getText().toString());
      config.setUserPassword(passwordEditText.getText().toString());
      config.setActiveDataStore(dataSourceSpinner.getSelectedItem().toString());
      config.setIsUsingSecurityPin(pinEnabledCheckBox.isChecked());
    }
  }

  private void setUIFromConfig() {
    dataSourceSpinner.setSelection(
        dataSourceSpinnerAdapter.getPosition(config.getActiveDataStore()));
    apiUrlEditText.setText(config.getApiUrl());
    usernameEditText.setText(config.getUserName());
    passwordEditText.setText(config.getUserPassword());
    pinEnabledCheckBox.setChecked(config.isUsingSecurityPin());
  }

  @Override
  protected void onPause() {
    setConfigFromUI();
    saveConfig();
    super.onPause();
  }

  @Override
  protected void onStop() {
    setConfigFromUI();
    saveConfig();
    super.onStop();
  }
}
