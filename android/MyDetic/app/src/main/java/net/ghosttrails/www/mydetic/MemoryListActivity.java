package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.MemoryDataList;
import net.ghosttrails.www.mydetic.api.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MemoryListActivity extends Activity
    implements AdapterView.OnItemClickListener {

  private ProgressDialog progressDialog;
  private MyDeticApplication app;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_memory_list);

    app = (MyDeticApplication) getApplicationContext();

    final ListView listView = (ListView) findViewById(R.id.listview);
    listView.setOnItemClickListener(this);

    final MemoriesAdapter adapter = new MemoriesAdapter(MemoryListActivity.this,
        app.getMemories());
    listView.setAdapter(adapter);
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
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Callback method to be invoked when an item in this AdapterView has
   * been clicked.
   * <p/>
   * Implementers can call getItemAtPosition(position) if they need
   * to access the data associated with the selected item.
   *
   * @param parent   The AdapterView where the click happened.
   * @param view     The view within the AdapterView that was clicked (this
   *                 will be a view provided by the adapter)
   * @param position The position of the view in the adapter.
   * @param id       The row id of the item that was clicked.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
                          long id) {
    MemoriesAdapter adapter = (MemoriesAdapter) parent.getAdapter();
    Date d = (Date) adapter.getItem(position);
    Intent intent = new Intent(this, MemoryDetailActivity.class);
    intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_DATE, Utils.isoFormat(d));
    intent.putExtra(MemoryDetailActivity.MEMORY_DETAIL_EDITMODE, "edit");
    startActivity(intent);
  }

  private class MemoriesAdapter extends BaseAdapter {

    private Context context;
    private MemoryDataList memories;
    private SparseArray<Date> positionMap;

    public MemoriesAdapter(Context context, MemoryDataList memories) {
      this.memories = memories;
      this.context = context;
      this.positionMap = new SparseArray<Date>();
      int idx = 0;
      for (Date d : this.memories.getDates()) {
        positionMap.put(idx, d);
        idx++;
      }
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
      return positionMap.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
      return positionMap.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      // TODO: This is lazy. Replace with a custom layout for list items.

      TwoLineListItem twoLineListItem;

      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        twoLineListItem = (TwoLineListItem) inflater.inflate(
            android.R.layout.simple_list_item_2, null);
      } else {
        twoLineListItem = (TwoLineListItem) convertView;
      }

      TextView text1 = twoLineListItem.getText1();
      TextView text2 = twoLineListItem.getText2();

      Date memoryDate = positionMap.get(position);
      text1.setText(Utils.isoFormat(memoryDate));

      // Use the first line of the memory text if we know it.
      MemoryData memoryData = app.getCachedMemory(memoryDate);
      if (memoryData != null) {
        text2.setEllipsize(TextUtils.TruncateAt.END);
        text2.setSingleLine();
        text2.setText(memoryData.getMemoryText());
      } else {
        text2.setText("");
      }

      return twoLineListItem;
    }

  }

}
