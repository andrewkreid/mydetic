package net.ghosttrails.www.mydetic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.ghosttrails.www.mydetic.api.InRamMemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.SampleSetPopulator;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;


public class HomeActivity extends AppCompatActivity {

  private ProgressDialog progressDialog;
  private MyDeticApplication app;
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    app = (MyDeticApplication) getApplicationContext();

    mRecyclerView = (RecyclerView) findViewById(R.id.home_cardview);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    // specify an adapter (see also next example)
    mAdapter = new MemoryCardviewAdaptor(app);
    mRecyclerView.setAdapter(mAdapter);

    app.getApi().getMemories(app.getUserId(), new MemoryApi.MemoryListListener() {
      @Override
      public void onApiResponse(MemoryDataList memories) {
        MemoryDataList appMemories = app.getMemories();
        appMemories.clear();
        try {
          appMemories.mergeFrom(memories);
        } catch (MyDeticException e) {
          AppUtils.smallToast(getApplicationContext(), e.getMessage());
        }
      }

      @Override
      public void onApiError(MyDeticException e) {
        AppUtils.smallToast(getApplicationContext(), e.getMessage());
      }
    });
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
    // TODO: Think about menu layout. Should settings be available from all
    // TODO: activities? Should Home be on the menu?
    int id = item.getItemId();

    switch (id) {
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.action_list:
        startActivity(new Intent(this, MemoryListActivity.class));
        return true;
      case R.id.action_new:
        Intent intent = new Intent(this, MemoryDetailActivity.class);
        intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "new");
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
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
}
