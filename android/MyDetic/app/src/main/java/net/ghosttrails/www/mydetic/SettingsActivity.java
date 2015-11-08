package net.ghosttrails.www.mydetic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
  private Button setPinButton;
  private EditText enterPin1EditText;
  private EditText enterPin2EditText;
  private TextView pinMessageTextView;

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

  private class PinTextChangeListener implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      // Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
      // Compare the PIN edit fields and update the message label.
      String pin1Text = enterPin1EditText.getText().toString();
      String pin2Text = enterPin2EditText.getText().toString();
      pinMessageTextView.setTextColor(Color.RED);
      if (!pin1Text.equals(pin2Text)) {
        pinMessageTextView.setText("PINs don't match");
      } else if (!isValidPIN(pin1Text) || !isValidPIN(pin2Text)) {
        pinMessageTextView.setText("PIN not valid");
      } else {
        pinMessageTextView.setTextColor(Color.GREEN);
        pinMessageTextView.setText("OK");
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
    setPinButton = (Button) findViewById(R.id.setPin);
    enterPin1EditText = (EditText) findViewById(R.id.enterPin1);
    enterPin2EditText = (EditText) findViewById(R.id.enterPin2);
    pinMessageTextView = (TextView) findViewById(R.id.pinMessage);

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


    // add button listener
    setPinButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
        View setPinView = li.inflate(R.layout.pin_entry_dlg, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            SettingsActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(setPinView);

        final EditText userInput = (EditText) setPinView
            .findViewById(R.id.enterPin);

        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // get user input and set it to result
                    // edit text
                    //result.setText(userInput.getText());
                  }
                })
            .setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                  }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        userInput.requestFocus();

      }
    });

    enterPin1EditText.addTextChangedListener();

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
      if (pinEnabledCheckBox.isChecked()) {
        // Only update the PIN if both text boxes match and are 4-digit numbers.
        String candidatePin = enterPin1EditText.getText().toString();
        if (candidatePin.equals(enterPin2EditText.getText().toString())
            && isValidPIN(candidatePin)) {
          config.setSecurityPin(candidatePin);
        } else {

        }
      }
    }
  }

  /**
   * Check that a PIN is valid. Has to be 4 digits.
   * @param pin the pin to check
   * @return true if valid, false otherwise.
   */
  private boolean isValidPIN(String pin) {
    if (pin.length() != 4) {
      return false;
    }
    for (int i = 0; i < 4; i++) {
      if (!Character.isDigit(pin.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private void setUIFromConfig() {
    dataSourceSpinner.setSelection(
        dataSourceSpinnerAdapter.getPosition(config.getActiveDataStore()));
    apiUrlEditText.setText(config.getApiUrl());
    usernameEditText.setText(config.getUserName());
    passwordEditText.setText(config.getUserPassword());
    pinEnabledCheckBox.setChecked(config.isUsingSecurityPin());
    enterPin1EditText.setEnabled(config.isUsingSecurityPin());
    enterPin2EditText.setEnabled(config.isUsingSecurityPin());
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
