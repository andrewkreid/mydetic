package net.ghosttrails.www.mydetic;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;


public class MemoryDetailActivity extends ActionBarActivity
  implements DatePickerDialog.OnDateSetListener {

  public static final String MEMORY_DETAIL_DATE =
      "net.ghosttrails.mydetic.MemoryDetailDate";
  public static final String MEMORY_DETAIL_EDITMODE =
      "net.ghosttrails.mydetic.MemoryDetailEditMode";

  private Date memoryDate;
  private MemoryData memoryData;
  private TextView dateTextView;
  private EditText memoryEditText;
  private Button saveButton;
  private Button refreshButton;

  /**
   * is this activity for creating a new memory or editing an existing one
   */
  public enum MemoryDetailMode {
    MODE_NEW,
    MODE_EXISTING
  }

  private MemoryDetailMode editMode;
  private boolean hasLoadedMemory;

  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_detail);

    progressDialog = new ProgressDialog(MemoryDetailActivity.this);
    progressDialog.setTitle("Processing...");
    progressDialog.setMessage("Please wait.");
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(true);

    // Will be set to true the first time the memory is loaded. This is so we
    // don't accidentally overwrite a memory that didn't load with a new one.
    hasLoadedMemory = false;

    Intent intent = getIntent();
    String memoryDateStr = intent.getStringExtra(MEMORY_DETAIL_DATE);
    if (memoryDateStr != null) {
      try {
        memoryDate = Utils.parseIsoDate(memoryDateStr);
      } catch (ParseException e) {
        // TODO: Do something here, but what?
        Log.e("MemoryDetailActivity", "Could not parse ["
            + memoryDateStr + "] as Date");
      }
    }

    String editModeStr = intent.getStringExtra(MEMORY_DETAIL_EDITMODE);
    if("edit".equals(editModeStr)) {
      editMode = MemoryDetailMode.MODE_EXISTING;
    } else {
      editMode = MemoryDetailMode.MODE_NEW;
    }

    memoryEditText = (EditText) this.findViewById(R.id.memory_text);
    memoryEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                    int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence,
                                int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        // Enable save button if we've typed some text.
        saveButton.setEnabled(memoryEditText.getText().length() > 0);
      }
    });

    refreshButton = (Button) this.findViewById(R.id.memory_refresh);
    saveButton = (Button) this.findViewById(R.id.memory_save);
    saveButton.setEnabled(false);

    dateTextView = (TextView) this.findViewById(R.id.memory_title);
    dateTextView.setText("Select Date...");

    if (editMode == MemoryDetailMode.MODE_EXISTING) {
      // Load the memory.
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      memoryData = app.getCachedMemory(memoryDate);
      if (memoryData == null) {
        new FetchMemoryTask().execute(memoryDate);
      } else {
        updateUIFromData();
      }
    }
    updateUIFromData();
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

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void hideKeyboard() {
    // Check if no view has focus:
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager inputManager = (InputMethodManager) this
          .getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(view.getWindowToken(),
          InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }

  /**
   * Enable and disable bits of the UI depending on what state the Activity
   * is in.
   */
  private void updateUIFromData() {
    if(memoryDate != null) {
      // TODO: Nicer date formatting (day of week etc.)
      dateTextView.setText(Utils.isoFormat(memoryDate));
    }
    // If there's a memoryData, update the text from it.
    if (memoryData != null) {
      memoryEditText.setText(memoryData.getMemoryText());
    }
    // save is enabled unless there is no memory text OR if we're in edit mode
    // and haven't loaded the memory yet.
    if(editMode == MemoryDetailMode.MODE_EXISTING && !hasLoadedMemory) {
      saveButton.setEnabled(false);
    } else {
      saveButton.setEnabled(memoryEditText.getText().length() > 0);
    }
    // Refresh is disabled in MODE_NEW
    refreshButton.setEnabled(editMode == MemoryDetailMode.MODE_EXISTING);
  }

  /**
   * Handle taps on the "Save" button. Saves the current memory text using
   * the API.
   *
   * @param view Button that was clicked.
   */
  public void saveClicked(View view) {
    hideKeyboard();
    if (memoryData == null) {
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      memoryData = new MemoryData(app.getUserId(),
          memoryEditText.getText().toString(), memoryDate);
    } else {
      memoryData.setMemoryText(memoryEditText.getText().toString());
    }
    new SaveMemoryTask().execute(memoryData);
  }

  public void refreshClicked(View view) {
    // TODO: warn about overwriting changes (also when closing activity).
    if (memoryDate != null) {
      new FetchMemoryTask().execute(memoryDate);
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

  @Override
  /**
   * Called when the user selects a date from the date picker in MODE_NEW.
   * Checks if a memory exists on that date.
   */
  public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    memoryDate = new GregorianCalendar(year, month, day).getTime();
    MyDeticApplication app = (MyDeticApplication) getApplicationContext();
    if (app.getMemories().hasDate(memoryDate)) {
      // There is already a memory on this date. Switch to edit mode and load
      // it.
      editMode = MemoryDetailMode.MODE_EXISTING;
      memoryData = app.getCachedMemory(memoryDate);
      if (memoryData == null) {
        new FetchMemoryTask().execute(memoryDate);
      } else {
        updateUIFromData();
      }
    } else {
      updateUIFromData();
    }
  }

  private class SaveMemoryTask extends AsyncTask<MemoryData, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      progressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean saveSuccessfull) {
      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }
      if (saveSuccessfull) {
        // Once we've saved, we're in edit mode.
        editMode = MemoryDetailMode.MODE_EXISTING;
        hasLoadedMemory = true;
        MyDeticApplication app = (MyDeticApplication) getApplicationContext();
        // TODO: Add to application memory list.
      }
      updateUIFromData();
    }

    @Override
    protected Boolean doInBackground(MemoryData... memoryDatas) {
      MemoryData memoryData = memoryDatas[0];
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      try {
        app.getApi().putMemory(app.getUserId(), memoryData);
        app.setCachedMemory(memoryData);
      } catch (MyDeticWriteFailedException e) {
        // TODO: Decide what to do about read/write errors globally.
        Log.e("MemoryDetailActivity", "Failed to save memory", e);
        return false;
      }
      return true;
    }
  }

  /**
   * Background task to fetch MemoryData objects from the API.
   */
  private class FetchMemoryTask extends AsyncTask<Date, Void, MemoryData> {

    @Override
    protected void onPreExecute() {
      progressDialog.show();
    }

    @Override
    protected void onPostExecute(MemoryData memoryData) {
      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }
      if (memoryData == null) {
        // Couldn't be fetched
        // TODO: propagate error info back here somehow for nicer messages.
        Context context = getApplicationContext();
        CharSequence text = "Failed to fetch memory";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        // TODO: We need to distinguish between a failed load and a date that
        // TODO: has no memory yet, so that we don't overwrite an existing one.
      } else {
        MyDeticApplication app = (MyDeticApplication) getApplicationContext();
        MemoryDetailActivity.this.memoryData = memoryData;
        app.setCachedMemory(memoryData);
        hasLoadedMemory = true;
      }
      updateUIFromData();
    }

    @Override
    protected MemoryData doInBackground(Date... params) {
      Date memoryDate = params[0];
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      try {
        return app.getApi().getMemory(app.getUserId(), memoryDate);
      } catch (MyDeticException e) {
        Log.e("MemoryDetailActivity", e.getMessage());
      }
      return null;
    }
  }


}
