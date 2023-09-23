package net.ghosttrails.www.mydetic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends LockableActivity {

  private Spinner dataSourceSpinner;
  private ArrayAdapter<String> dataSourceSpinnerAdapter;
  private EditText apiUrlEditText;
  private EditText usernameEditText;
  private EditText passwordEditText;
  private CheckBox pinEnabledCheckBox;
  private EditText enterPin1EditText;
  private EditText enterPin2EditText;
  private TextView pinMessageTextView;
  private CheckBox reminderEnabledCheckBox;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    dataSourceSpinner = findViewById(R.id.dataStoreSpinner);
    apiUrlEditText = findViewById(R.id.apiUrlEditText);
    usernameEditText = findViewById(R.id.apiUserNameEditText);
    passwordEditText = findViewById(R.id.passwordEditText);
    pinEnabledCheckBox = findViewById(R.id.enablePinLock);
    enterPin1EditText = findViewById(R.id.enterPin1);
    enterPin2EditText = findViewById(R.id.enterPin2);
    pinMessageTextView = findViewById(R.id.pinMessage);
    reminderEnabledCheckBox = findViewById(R.id.enableReminderCheckBox);

    PersistOnFocusLossListener focusLossListener = new PersistOnFocusLossListener();
    apiUrlEditText.setOnFocusChangeListener(focusLossListener);
    usernameEditText.setOnFocusChangeListener(focusLossListener);
    passwordEditText.setOnFocusChangeListener(focusLossListener);
    pinEnabledCheckBox.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            setConfigFromUI();
            MemoryAppState.getInstance().getConfig().saveConfig(getApplicationContext());
          }
        });

    dataSourceSpinnerAdapter =
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            MemoryAppState.getInstance().getConfig().getDataStoreList());
    dataSourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
    dataSourceSpinner.setAdapter(dataSourceSpinnerAdapter);

    enterPin1EditText.addTextChangedListener(new PinTextChangeListener());
    enterPin2EditText.addTextChangedListener(new PinTextChangeListener());

    reminderEnabledCheckBox.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setOrRemoveReminder(reminderEnabledCheckBox.isChecked());
      }
    });

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

  /** Tap handler for the "Clear Cache" button. Empties the SQLite cache DB */
  public void clearCacheClicked(View view) {
    MemoryAppState.getInstance().clearMemoryCache();
    AppUtils.smallToast(getApplicationContext(), "Cache cleared.");
  }

  private void setConfigFromUI() {
    MyDeticConfig config = MemoryAppState.getInstance().getConfig();
    if (config != null) {
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
          // If the current entered PINs don't match, then turn off the PIN lock to guard against
          // using an old PIN the user may have forgotten.
          config.setIsUsingSecurityPin(false);
          pinEnabledCheckBox.setChecked(false);
          config.setSecurityPin("");
        }
      }
      config.setIsReminderEnabled(reminderEnabledCheckBox.isChecked());
      setOrRemoveReminder(reminderEnabledCheckBox.isChecked());
    }
  }

  // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.
  private final ActivityResultLauncher<String> requestPermissionLauncher =
          registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
              // Permission is granted. Continue the action or workflow in your
              // app.
              AppUtils.setReminderNotification(SettingsActivity.this);
            } else {
              // Explain to the user that the feature is unavailable because the
              // feature requires a permission that the user has denied. At the
              // same time, respect the user's decision. Don't link to system
              // settings in an effort to convince the user to change their
              // decision.
              AppUtils.smallToast(SettingsActivity.this, "Notifications Denied!");
            }
          });

  /**
   * Set or remove the daily reminder to enter your memories.
   *
   * @param shouldSet true if the reminder should be set, false otherwise.
   */
  private void setOrRemoveReminder(boolean shouldSet) {
    if (shouldSet) {
      if (ContextCompat.checkSelfPermission(
              this, Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED) {
        // You can use the API that requires the permission.
        AppUtils.setReminderNotification(this);
      } else {
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS);
      }
    } else {
      // If the alarm has been set, cancel it.
      AppUtils.cancelReminderNotification(this);
    }
  }

  /**
   * Check that a PIN is valid. Has to be 4 digits.
   *
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
    MyDeticConfig config = MemoryAppState.getInstance().getConfig();
    dataSourceSpinner.setSelection(
        dataSourceSpinnerAdapter.getPosition(config.getActiveDataStore()));
    apiUrlEditText.setText(config.getApiUrl());
    usernameEditText.setText(config.getUserName());
    passwordEditText.setText(config.getUserPassword());
    pinEnabledCheckBox.setChecked(config.isUsingSecurityPin());
    enterPin1EditText.setText(config.getSecurityPin());
    enterPin2EditText.setText(config.getSecurityPin());
    reminderEnabledCheckBox.setChecked(config.isReminderEnabled());
  }

  @Override
  protected void onPause() {
    setConfigFromUI();
    MemoryAppState.getInstance().getConfig().saveConfig(getApplicationContext());
    super.onPause();
  }

  /** Focus loss listener that saves the config */
  private class PersistOnFocusLossListener implements View.OnFocusChangeListener {

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
      if (!hasFocus) {
        setConfigFromUI();
        MemoryAppState.getInstance().getConfig().saveConfig(getApplicationContext());
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
        pinMessageTextView.setText(R.string.pins_dont_match);
      } else if (!isValidPIN(pin1Text) || !isValidPIN(pin2Text)) {
        pinMessageTextView.setText(R.string.pin_not_valid);
      } else {
        pinMessageTextView.setTextColor(Color.argb(255, 0, 128, 0));
        pinMessageTextView.setText(R.string.ok);
      }
    }
  }
}
