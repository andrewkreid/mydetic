package net.ghosttrails.www.mydetic;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.joda.time.LocalDate;

public class HomeActivity extends LockableActivity {

  private static String TAG = "MyDeticHomeActivity";
  private RecyclerView mRecyclerView;
  private MemoryCardviewAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    // Disable screenshots in activity switcher.
    getWindow()
        .setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

    MemoryAppState appState = MemoryAppState.getInstance();

    mRecyclerView = findViewById(R.id.home_cardview);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    // specify an adapter.
    mAdapter =
        new MemoryCardviewAdapter(
            new CustomItemClickListener() {
              @Override
              public void onItemClick(View v, int position, LocalDate memoryDate) {
                // Clicking on a Memory card takes you to the detail activity for that date.
                Intent intent = new Intent(HomeActivity.this, MemoryDetailActivity.class);
                intent.putExtra(
                    MemoryDetailActivity.MEMORY_DETAIL_DATE, Utils.isoFormat(memoryDate));
                if (MemoryAppState.getInstance().getMemories().hasDate(memoryDate)) {
                  intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "edit");
                } else {
                  intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "new");
                }
                // setTransitioningToAppActivity(true);
                startActivity(intent);
              }
            });
    mAdapter.setCardHistoryType(appState.getConfig().getListSetting());
    mRecyclerView.setAdapter(mAdapter);

    // The SQLite cache is initialized asynchronously, so it may not be available yet.
    appState.onCacheReady(
        new Runnable() {
          @Override
          public void run() {
            try {
              MemoryAppState.getInstance().loadMemoryDatesFromCache();
              mAdapter.recalculatePastMemoryDates();
              mAdapter.notifyDataSetChanged();
              mRecyclerView.invalidate();
            } catch (MyDeticException e) {
              Log.e(TAG, e.getMessage());
            }
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
    MemoryAppState appState = MemoryAppState.getInstance();
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
      case R.id.action_reload:
        appState.refreshSettingsFromConfig(this);
        appState.loadMemoryDatesFromApi(
            this,
            new MemoryApi.MemoryListListener() {
              @Override
              public void onApiResponse(MemoryDataList memories) {
                mAdapter.recalculatePastMemoryDates();
                mAdapter.notifyDataSetChanged();
                mRecyclerView.invalidate();
              }

              @Override
              public void onApiError(MyDeticException exception) {
                mAdapter.recalculatePastMemoryDates();
                mAdapter.notifyDataSetChanged();
                mRecyclerView.invalidate();
              }
            });
        return true;
      case R.id.action_thepast:
        appState.getConfig().setListSetting(MyDeticConfig.LISTSETTING_THEPAST);
        appState.getConfig().saveConfig(getApplicationContext());
        mAdapter.setCardHistoryType(MyDeticConfig.LISTSETTING_THEPAST);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();
        return true;
      case R.id.action_thisweek:
        appState.getConfig().setListSetting(MyDeticConfig.LISTSETTING_THISWEEK);
        appState.getConfig().saveConfig(getApplicationContext());
        mAdapter.setCardHistoryType(MyDeticConfig.LISTSETTING_THISWEEK);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mRecyclerView.invalidate();

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      notificationManager.cancelAll();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    mAdapter.notifyDataSetChanged();
    mRecyclerView.invalidate();
  }
}
