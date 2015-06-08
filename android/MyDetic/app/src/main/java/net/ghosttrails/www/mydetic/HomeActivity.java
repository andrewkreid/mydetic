package net.ghosttrails.www.mydetic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.ghosttrails.www.mydetic.api.InRamMemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.SampleSetPopulator;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;


public class HomeActivity extends ActionBarActivity {

  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    new FetchMemoryListTask().execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_home, menu);
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

  public void memoryListClicked(View view) {
    Intent intent = new Intent(this, MemoryListActivity.class);
    startActivity(intent);
  }

  public void memoryNewClicked(View view) {
    Intent intent = new Intent(this, MemoryDetailActivity.class);
    intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "new");
    startActivity(intent);
  }

  private class FetchMemoryListTask extends AsyncTask<Void, Void, MemoryDataList> {

    @Override
    protected MemoryDataList doInBackground(Void... voids) {
      // refetch the memories from the API.
      MyDeticApplication app = (MyDeticApplication) getApplicationContext();
      MemoryDataList memories = app.getMemories();
      memories.clear();
      try {
        memories.mergeFrom(app.getApi().getMemories(app.getUserId()));
      } catch (MyDeticException e) {
        // something went wrong fetching memories initially
        // TODO: what's the right thing to do here.
        e.printStackTrace();
      }
      return memories;
    }

    @Override
    protected void onPostExecute(MemoryDataList memories) {
      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }
    }

    @Override
    protected void onPreExecute() {
      progressDialog = new ProgressDialog(HomeActivity.this);
      progressDialog.setTitle("Processing...");
      progressDialog.setMessage("Please wait.");
      progressDialog.setCancelable(false);
      progressDialog.setIndeterminate(true);
      progressDialog.show();
    }
  }
}
