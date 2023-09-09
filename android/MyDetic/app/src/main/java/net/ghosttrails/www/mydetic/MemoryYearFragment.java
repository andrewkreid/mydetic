package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A fragment representing a list of years for which there are memories
 *
 * <p>Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MemoryYearFragment extends ListFragment {

  private OnFragmentInteractionListener mListener;

  private List<Integer> mYearsWithMemories;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * screen orientation changes).
   */
  public MemoryYearFragment() {}

  public static MemoryYearFragment newInstance() {
    return new MemoryYearFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mYearsWithMemories = buildYearList();

    setListAdapter(
        new ArrayAdapter<>(
            getActivity(),
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            mYearsWithMemories));
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(
          activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * Build a list of the years that have at least one memory.
   *
   * @return a sorted List<Integer>
   */
  List<Integer> buildYearList() {
    ArrayList<Integer> retVal = new ArrayList<>();
    MemoryAppState appState = MemoryAppState.getInstance();
    if (appState != null) {
      Set<Integer> uniqueYears = appState.getMemories().getYears();
      retVal.addAll(uniqueYears);
      Collections.sort(retVal);
    }
    return retVal;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.
      mListener.onYearSelected(mYearsWithMemories.get(position));
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
    void onYearSelected(int year);
  }
}
