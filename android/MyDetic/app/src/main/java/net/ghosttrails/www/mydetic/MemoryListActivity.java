package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import net.ghosttrails.www.mydetic.api.MemoryApi;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MemoryListActivity extends Activity
    implements MemoryYearFragment.OnFragmentInteractionListener,
    MemoryMonthFragment.OnFragmentInteractionListener,
    MemoryDayFragment.OnFragmentInteractionListener,
    MemoryListFragmentDataProvider {

  private int year;
  private int month;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_list);

    // Disable screenshots in activity switcher.
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE);

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    if (findViewById(R.id.fragment_container) != null) {

      // However, if we're being restored from a previous state,
      // then we don't need to do anything and should return or else
      // we could end up with overlapping fragments.
      if (savedInstanceState != null) {
        year = savedInstanceState.getInt("year", 2015);
        month = savedInstanceState.getInt("month", 1);
        return;
      }

      // Create a new Fragment to be placed in the activity layout
      MemoryYearFragment yearFragment = MemoryYearFragment.newInstance();

      // In case this activity was started with special instructions from an
      // Intent, pass the Intent's extras to the fragment as arguments
      yearFragment.setArguments(getIntent().getExtras());

      // Add the fragment to the 'fragment_container' FrameLayout
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, yearFragment).commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_memory_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch(item.getItemId()) {
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("year", year);
    outState.putInt("month", month);
  }

  @Override
  public void onYearSelected(int year) {
    Log.i("MemoryListActivity", String.format("Selected year %d", year));

    this.year = year;

    // Create fragment.
    // TODO: Check if fragment already loaded?
    MemoryMonthFragment newFragment = MemoryMonthFragment.newInstance(year);
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack so the user can navigate back
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.addToBackStack(null);

    // Commit the transaction
    transaction.commit();
  }

  @Override
  public void onYearMonthSelected(int year, int month) {
    this.year = year;
    this.month = month;

    Log.i("MemoryListActivity", String.format("Selected year %d and month %d", year, month));
    // Create fragment.
    // TODO: Check if fragment already loaded?
    MemoryDayFragment newFragment = MemoryDayFragment.newInstance(year, month);
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack so the user can navigate back
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.addToBackStack(null);

    // Commit the transaction
    transaction.commit();
  }

  @Override
  public void onDateSelected(Date d) {
    Log.i("MemoryListActivity", String.format("Selected date %s", Utils.isoFormat(d)));
    Intent intent = new Intent(this, MemoryDetailActivity.class);
    intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_DATE, Utils.isoFormat(d));
    intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "edit");
    startActivity(intent);
    // TODO: Refresh list fragments on returning to the activity.
  }

  @Override
  public MemoryAppInterface getAppInterface() {
    return (MemoryAppInterface) getApplicationContext();
  }

  @Override
  public int getMemoryListYear() {
    return year;
  }

  @Override
  public int getMemoryListMonth() {
    return month;
  }
}
