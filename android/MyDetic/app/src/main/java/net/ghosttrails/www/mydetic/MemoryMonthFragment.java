package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a list of months in a year for which there are memories
 *
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MemoryMonthFragment extends ListFragment {
  private OnFragmentInteractionListener mListener;

  private int mYear;
  private List<Integer> mMonthsWithMemories;

  public static MemoryMonthFragment newInstance(int year) {
    MemoryMonthFragment fragment = new MemoryMonthFragment();
    fragment.mYear = year;
    return fragment;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public MemoryMonthFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // setRetainInstance(true);

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnFragmentInteractionListener");
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
    MemoryListFragmentDataProvider provider = (MemoryListFragmentDataProvider)getActivity();
    mYear = provider.getMemoryListYear();
    mMonthsWithMemories = new ArrayList<>();

    MemoryAppState appState = MemoryAppState.getInstance();
    for(Integer i:appState.getMemories().getMonthsForYear(mYear)) {
      mMonthsWithMemories.add(i);
    }
    List<String> monthNames = new ArrayList<>();
    for(Integer month:mMonthsWithMemories) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, month);
      String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
      monthNames.add(monthName);
    }

    setListAdapter(new ArrayAdapter<String>(getActivity(),
        android.R.layout.simple_list_item_1, android.R.id.text1, monthNames));
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
    mMonthsWithMemories = null;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.
      mListener.onYearMonthSelected(mYear, mMonthsWithMemories.get(position));
    }
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    void onYearMonthSelected(int year, int month);
  }
}
