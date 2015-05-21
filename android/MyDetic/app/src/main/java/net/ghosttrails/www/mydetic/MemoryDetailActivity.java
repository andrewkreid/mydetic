package net.ghosttrails.www.mydetic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.NoMemoryFoundException;

import java.text.ParseException;
import java.util.Date;


public class MemoryDetailActivity extends ActionBarActivity {

  private MemoryData memoryData;
  private TextView dateTextView;
  private EditText memoryEditText;

  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_detail);

    Intent intent = getIntent();
    String memoryDateStr = intent.getStringExtra(MemoryListActivity.MEMORY_DETAIL_DATE);

    memoryEditText = (EditText)this.findViewById(R.id.memory_text);

    dateTextView = (TextView)this.findViewById(R.id.memory_title);
    dateTextView.setText("Select Date...");
    // TODO: tapping opens date picker.

    if (memoryDateStr != null) {

      try {
        Date memoryDate = Utils.parseIsoDate(memoryDateStr);

        // fetch the memory
        MyDeticApplication app = (MyDeticApplication)getApplicationContext();

        // TODO: Progress spinner while the memory loads.
        new FetchMemoryTask().execute(memoryDate);

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

  /**
   * Handle taps on the "Save" button. Saves the current memory text using
   * the API.
   * @param view Button that was clicked.
   */
  public void saveClicked(View view) {
    // TODO: Save memory text
  }

  public void refreshClicked(View view) {
    // TODO: Refresh memory.
  }

  /**
   * Background task to fetch MemoryData objects from the API.
   */
  private class FetchMemoryTask extends AsyncTask<Date, Void, MemoryData> {

    @Override
    protected void onPreExecute() {
      progressDialog = new ProgressDialog(MemoryDetailActivity.this);
      progressDialog.setTitle("Processing...");
      progressDialog.setMessage("Please wait.");
      progressDialog.setCancelable(false);
      progressDialog.setIndeterminate(true);
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
        dateTextView.setText("Could not fetch");
        memoryEditText.setText("");
      } else {
        MemoryDetailActivity.this.memoryData = memoryData;
        // TODO: Nicer date formatting (day of week etc.)
        dateTextView.setText(Utils.isoFormat(memoryData.getMemoryDate()));
        memoryEditText.setText(memoryData.getMemoryText());
      }
    }

    @Override
    protected MemoryData doInBackground(Date... params) {
      Date memoryDate = params[0];
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      try {
        return app.getApi().getMemory(app.getUserId(), memoryDate);
      } catch (NoMemoryFoundException e) {
        Log.e("MemoryDetailActivity", e.getMessage());
      }

      return null;
    }
  }
}
