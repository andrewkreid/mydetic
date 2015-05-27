package net.ghosttrails.www.mydetic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticNoMemoryFoundException;
import net.ghosttrails.www.mydetic.exceptions.MyDeticWriteFailedException;

import java.text.ParseException;
import java.util.Date;


public class MemoryDetailActivity extends ActionBarActivity {

  private MemoryData memoryData;
  private TextView dateTextView;
  private EditText memoryEditText;
  private Button saveButton;
  private Button refreshButton;

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

    Intent intent = getIntent();
    String memoryDateStr = intent.getStringExtra(MemoryListActivity
        .MEMORY_DETAIL_DATE);

    memoryEditText = (EditText) this.findViewById(R.id.memory_text);
    refreshButton = (Button) this.findViewById(R.id.memory_refresh);
    saveButton = (Button) this.findViewById(R.id.memory_save);

    dateTextView = (TextView) this.findViewById(R.id.memory_title);
    dateTextView.setText("Select Date...");
    // TODO: tapping opens date picker.

    if (memoryDateStr != null) {
      try {
        MyDeticApplication app = (MyDeticApplication) getApplicationContext();
        Date memoryDate = Utils.parseIsoDate(memoryDateStr);

        memoryData = app.getCachedMemory(memoryDate);
        if(memoryData == null) {
          new FetchMemoryTask().execute(memoryDate);
        } else {
          updateUIFromData();
        }
      } catch (ParseException e) {
        Log.e("MemoryDetailActivity", "Could not parse ["
            + memoryDateStr + "] as Date");
      }
    }
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

  private void updateUIFromData() {
    // TODO: disable save button if we don't have a memory.
    if (memoryData == null) {
      dateTextView.setText("No Data");
      memoryEditText.setText("");
    } else {
      // TODO: Nicer date formatting (day of week etc.)
      dateTextView.setText(Utils.isoFormat(memoryData.getMemoryDate()));
      memoryEditText.setText(memoryData.getMemoryText());
    }
  }

  /**
   * Handle taps on the "Save" button. Saves the current memory text using
   * the API.
   *
   * @param view Button that was clicked.
   */
  public void saveClicked(View view) {
    hideKeyboard();
    memoryData.setMemoryText(memoryEditText.getText().toString());
    new SaveMemoryTask().execute(memoryData);
  }

  public void refreshClicked(View view) {
    new FetchMemoryTask().execute(MemoryDetailActivity.this.memoryData.getMemoryDate());
  }

  private class SaveMemoryTask extends AsyncTask<MemoryData, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      progressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }
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
      } else {
        MyDeticApplication app = (MyDeticApplication) getApplicationContext();
        MemoryDetailActivity.this.memoryData = memoryData;
        app.setCachedMemory(memoryData);
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
