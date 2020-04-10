package net.ghosttrails.www.mydetic;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import net.ghosttrails.www.mydetic.api.MemoryData;
import net.ghosttrails.www.mydetic.api.Utils;
import org.joda.time.LocalDate;

/**
 * A fragment representing a list of days in a month for which there are memories
 *
 * <p>Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MemoryDayFragment extends ListFragment {
  private OnFragmentInteractionListener mListener;

  private int mYear;
  private int mMonth;
  private List<LocalDate> mDatesWithMemories;
  private MemoriesAdapter mAdapter;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * screen orientation changes).
   */
  public MemoryDayFragment() {}

  public static MemoryDayFragment newInstance(int year, int month) {
    MemoryDayFragment fragment = new MemoryDayFragment();
    fragment.mYear = year;
    fragment.mMonth = month;
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Context activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(
          activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    // Refresh the list on resume because we might be coming back from the memory detail view,
    // so memory text might have been loaded or changed.
    if (mAdapter != null) {
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Configure things here because MyDeticListActivity restores its state in onCreate, and this
   * method (unlike onCreate/onAttach) is called after that.
   *
   * @param savedInstanceState saved state
   */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    MemoryListFragmentDataProvider provider = (MemoryListFragmentDataProvider) getActivity();
    mYear = provider.getMemoryListYear();
    mMonth = provider.getMemoryListMonth();

    mDatesWithMemories = new ArrayList<>();
    MemoryAppState appState = MemoryAppState.getInstance();

    for (LocalDate d : appState.getMemories().getDatesForMonth(mYear, mMonth)) {
      mDatesWithMemories.add(d);
    }
    mAdapter = new MemoriesAdapter(getActivity(), mDatesWithMemories);
    setListAdapter(mAdapter);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.
      mListener.onDateSelected(mDatesWithMemories.get(position));
    }
  }

  /**
   * This interface must be implemented by activities that contain this fragment to allow an
   * interaction in this fragment to be communicated to the activity and potentially other fragments
   * contained in that activity.
   *
   * <p>See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with
   * Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    void onDateSelected(LocalDate d);
  }

  private class MemoriesAdapter extends BaseAdapter {

    private Context context;
    private List<LocalDate> mMemories;

    MemoriesAdapter(Context context, List<LocalDate> memories) {
      this.mMemories = memories;
      this.context = context;
    }

    @Override
    public int getCount() {
      return mMemories.size();
    }

    @Override
    public Object getItem(int position) {
      return mMemories.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      LinearLayout twoLineListItem;

      if (convertView == null) {
        LayoutInflater inflater =
            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        twoLineListItem = (LinearLayout) inflater.inflate(R.layout.day_list_item, null);
      } else {
        twoLineListItem = (LinearLayout) convertView;
      }

      TextView text1 = twoLineListItem.findViewById(R.id.topline);
      TextView text2 = twoLineListItem.findViewById(R.id.bottomline);

      LocalDate memoryDate = mMemories.get(position);
      text1.setText(Utils.isoFormat(memoryDate));

      // Use the first line of the memory text if we know it.
      MemoryAppState appState = MemoryAppState.getInstance();
      MemoryData memoryData = appState.getCachedMemory(memoryDate);
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
