package net.ghosttrails.www.mydetic;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;

import java.text.ParseException;
import java.util.Date;


public class MemoryDetailActivity extends ActionBarActivity {

  private MemoryData memoryData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_detail);

    Intent intent = getIntent();
    String memoryDateStr = intent.getStringExtra(MemoryListActivity.MEMORY_DETAIL_DATE);

    TextView dateText = (TextView)this.findViewById(R.id.memory_title);
    dateText.setText("Select Date...");
    // TODO: tapping opens date picker.

    if (memoryDateStr != null) {

      try {
        Date memorydate = Utils.parseIsoDate(memoryDateStr);
        // TODO: Nicer date formatting (day of week etc.)
        dateText.setText(Utils.isoFormat(memorydate));

        // fetch the memory
        MyDeticApplication app = (MyDeticApplication)getApplicationContext();

        // TODO: Asynchronous api call to fetch the memory.

      } catch (ParseException e) {
        Log.e("MemoryDetailActivity", "Could not parse [" + memoryDateStr + "] as Date");
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
}
