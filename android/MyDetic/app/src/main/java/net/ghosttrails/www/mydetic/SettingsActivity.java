package net.ghosttrails.www.mydetic;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import java.util.Calendar;


public class SettingsActivity extends LockableActivity {

    private MyDeticConfig config;
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
                pinMessageTextView.setText(R.string.pins_dont_match);
            } else if (!isValidPIN(pin1Text) || !isValidPIN(pin2Text)) {
                pinMessageTextView.setText(R.string.pin_not_valid);
            } else {
                pinMessageTextView.setTextColor(Color.argb(255, 0, 128, 0));
                pinMessageTextView.setText(R.string.ok);
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
        enterPin1EditText = (EditText) findViewById(R.id.enterPin1);
        enterPin2EditText = (EditText) findViewById(R.id.enterPin2);
        pinMessageTextView = (TextView) findViewById(R.id.pinMessage);
        reminderEnabledCheckBox = (CheckBox) findViewById(R.id.enableReminderCheckBox);

        loadConfig();

        PersistOnFocusLossListener focusLossListener = new PersistOnFocusLossListener();
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

        enterPin1EditText.addTextChangedListener(new PinTextChangeListener());
        enterPin2EditText.addTextChangedListener(new PinTextChangeListener());

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

    /**
     * Set or remove the daily reminder to enter your memories.
     *
     * @param shouldSet true if the reminder should be set, false otherwise.
     */
    private void setOrRemoveReminder(boolean shouldSet) {
        if (shouldSet) {
            AppUtils.setReminderNotification(this);
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
