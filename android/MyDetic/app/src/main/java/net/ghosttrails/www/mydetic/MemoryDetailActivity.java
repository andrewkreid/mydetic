package net.ghosttrails.www.mydetic;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.joda.time.LocalDate;

public class MemoryDetailActivity extends LockableActivity
    implements DatePickerDialog.OnDateSetListener {

  public static final String MEMORY_DETAIL_DATE = "net.ghosttrails.mydetic.MemoryDetailDate";
  public static final String MEMORY_DETAIL_EDITMODE =
      "net.ghosttrails.mydetic.MemoryDetailEditMode";

  private LocalDate memoryDate;
  private MemoryData memoryData;
  private TextView dateTextView;
  private TextView cacheStatusTextView;
  private EditText memoryEditText;
  private Button saveButton;
  private Button refreshButton;
  private MemoryDetailMode editMode;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_detail);

    // Disable screenshots in activity switcher.
    getWindow()
        .setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

    progressBar = this.findViewById(R.id.memory_detail_progress_bar);

    Intent intent = getIntent();
    String memoryDateStr = intent.getStringExtra(MEMORY_DETAIL_DATE);
    if (memoryDateStr != null) {
      try {
        memoryDate = Utils.parseIsoDate(memoryDateStr);
      } catch (IllegalArgumentException e) {
        // TODO: Do something here, but what?
        Log.e("MemoryDetailActivity", "Could not parse [" + memoryDateStr + "] as Date");
      }
    }

    String editModeStr = intent.getStringExtra(MEMORY_DETAIL_EDITMODE);
    if ("edit".equals(editModeStr)) {
      editMode = MemoryDetailMode.MODE_EXISTING;
    } else {
      editMode = MemoryDetailMode.MODE_NEW;
    }

    memoryEditText = findViewById(R.id.memory_text);
    memoryEditText.addTextChangedListener(
        new TextWatcher() {

          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable editable) {
            // Enable save button if we've typed some text.
            saveButton.setEnabled(memoryEditText.getText().length() > 0);
          }
        });

    cacheStatusTextView = findViewById(R.id.cache_status_text);

    refreshButton = findViewById(R.id.memory_refresh);
    saveButton = findViewById(R.id.memory_save);
    saveButton.setEnabled(false);

    dateTextView = findViewById(R.id.memory_title);
    dateTextView.setText(R.string.select_date);

    if (editMode == MemoryDetailMode.MODE_EXISTING) {
      // Load the memory.
      MemoryAppState appState = MemoryAppState.getInstance();
      memoryData = appState.getCachedMemory(memoryDate);
      if (memoryData == null) {
        setButtonsEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        appState
            .getApi()
            .getMemory(appState.getConfig().getUserName(), memoryDate, new FetchMemoryListener());
      } else {
        updateUIFromData();
      }
    }
    updateUIFromData();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    // TODO: It might not always be the case that going back from here stays in the app?
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_memory_detail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        return super.onOptionsItemSelected(item);
      case R.id.action_settings:
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void hideKeyboard() {
    // Check if no view has focus:
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager inputManager =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(
          view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }

  /** Enable and disable bits of the UI depending on what state the Activity is in. */
  private void updateUIFromData() {
    if (memoryDate != null) {
      // TODO: Nicer date formatting (day of week etc.)
      dateTextView.setText(Utils.isoFormat(memoryDate));
    }
    // If there's a memoryData, update the text from it.
    if (memoryData != null) {
      memoryEditText.setText(memoryData.getMemoryText());
    }
    saveButton.setEnabled(memoryEditText.getText().length() > 0);
    // Refresh is disabled in MODE_NEW
    refreshButton.setEnabled(editMode == MemoryDetailMode.MODE_EXISTING);
    // Alert the user if the Memory has not been saved to the API.
    if (memoryData != null && memoryData.getCacheState() == MemoryData.CACHESTATE_PENDING_SAVE) {
      cacheStatusTextView.setText(R.string.memory_unsaved);
    } else {
      cacheStatusTextView.setText("");
    }
  }

  /**
   * Handle taps on the "Save" button. Saves the current memory text using the API.
   *
   * @param view Button that was clicked.
   */
  public void saveClicked(View view) {
    hideKeyboard();
    MemoryAppState appState = MemoryAppState.getInstance();
    if (memoryData == null) {
      memoryData =
          new MemoryData(
              appState.getConfig().getUserName(), memoryEditText.getText().toString(), memoryDate);
    } else {
      memoryData.setMemoryText(memoryEditText.getText().toString());
    }

    // Cache the memory as pending so it still gets persisted even if there's no network etc.
    memoryData.setCacheState(MemoryData.CACHESTATE_PENDING_SAVE);
    try {
      appState.setCachedMemory(memoryData);
    } catch (MyDeticException e) {
      Log.e("MemoryDetailsActivity", e.getMessage());
      AppUtils.smallToast(getApplicationContext(), e.getMessage());
    }

    setButtonsEnabled(false);
    progressBar.setVisibility(View.VISIBLE);
    appState
        .getApi()
        .putMemory(appState.getConfig().getUserName(), memoryData, new SaveMemoryListener());
  }

  public void refreshClicked(View view) {
    // TODO: warn about overwriting changes (also when closing activity).
    MemoryAppState appState = MemoryAppState.getInstance();
    if (memoryDate != null) {
      setButtonsEnabled(false);
      progressBar.setVisibility(View.VISIBLE);
      appState
          .getApi()
          .getMemory(appState.getConfig().getUserName(), memoryDate, new FetchMemoryListener());
    }
  }

  public void dateFieldClicked(View view) {
    // Only allow date field to be edited when adding a new memory.
    if (editMode == MemoryDetailMode.MODE_NEW) {
      DatePickerFragment newFragment = new DatePickerFragment();
      newFragment.addListener(this);
      newFragment.show(getFragmentManager(), "datePicker");
    }
  }

  private void setButtonsEnabled(boolean isEnabled) {
    saveButton.setEnabled(isEnabled);
    refreshButton.setEnabled(isEnabled);
  }

  @Override
  /**
   * Called when the user selects a date from the date picker in MODE_NEW. Checks if a memory exists
   * on that date.
   */
  public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    memoryDate = new LocalDate(year, month, day);
    MemoryAppState appState = MemoryAppState.getInstance();
    if (appState.getMemories().hasDate(memoryDate)) {
      // There is already a memory on this date. Switch to edit mode and load
      // it.
      editMode = MemoryDetailMode.MODE_EXISTING;
      memoryData = appState.getCachedMemory(memoryDate);
      if (memoryData == null) {
        appState
            .getApi()
            .getMemory(appState.getConfig().getUserName(), memoryDate, new FetchMemoryListener());
      } else {
        updateUIFromData();
      }
    } else {
      updateUIFromData();
    }
  }

  /** is this activity for creating a new memory or editing an existing one */
  public enum MemoryDetailMode {
    MODE_NEW,
    MODE_EXISTING
  }

  private class SaveMemoryListener implements MemoryApi.SingleMemoryListener {
    @Override
    public void onApiResponse(MemoryData memory) {
      MemoryAppState appState = MemoryAppState.getInstance();
      setButtonsEnabled(true);
      progressBar.setVisibility(View.GONE);
      // Once we've saved, we're in edit mode.
      editMode = MemoryDetailMode.MODE_EXISTING;
      memoryData = memory;
      try {
        memoryData.setCacheState(MemoryData.CACHESTATE_SAVED);
        appState.setCachedMemory(memory);
      } catch (MyDeticException e) {
        Log.e("MemoryDetailsActivity", e.getMessage());
        AppUtils.smallToast(getApplicationContext(), e.getMessage());
      }
      updateUIFromData();
    }

    @Override
    public void onApiError(MyDeticException exception) {
      setButtonsEnabled(true);
      progressBar.setVisibility(View.GONE);
      AppUtils.smallToast(getApplicationContext(), exception.getMessage());
      updateUIFromData();
    }
  }

  private class FetchMemoryListener implements MemoryApi.SingleMemoryListener {

    @Override
    public void onApiResponse(MemoryData memory) {
      setButtonsEnabled(true);
      progressBar.setVisibility(View.GONE);
      if (memory != null) {
        MemoryAppState appState = MemoryAppState.getInstance();
        MemoryDetailActivity.this.memoryData = memory;
        try {
          appState.setCachedMemory(memory);
        } catch (MyDeticException e) {
          Log.e("MemoryDetailsActivity", e.getMessage());
          AppUtils.smallToast(getApplicationContext(), e.getMessage());
        }
        editMode = MemoryDetailMode.MODE_EXISTING;
      }
      updateUIFromData();
    }

    @Override
    public void onApiError(MyDeticException exception) {
      setButtonsEnabled(true);
      progressBar.setVisibility(View.GONE);
      // TODO: We need to distinguish between a failed load and a date that
      // TODO: has no memory yet, so that we don't overwrite an existing one.
      AppUtils.smallToast(getApplicationContext(), exception.getMessage());
    }
  }
}
