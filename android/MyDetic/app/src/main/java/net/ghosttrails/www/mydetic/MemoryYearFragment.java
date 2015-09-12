package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import net.ghosttrails.www.mydetic.api.Utils;
import net.ghosttrails.www.mydetic.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A fragment representing a list of years for which there are memories
 *
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MemoryYearFragment extends Fragment implements AbsListView.OnItemClickListener {

  private MemoryAppInterface mApp;

  private OnFragmentInteractionListener mListener;

  private List<Integer> mYearsWithMemories;

  /**
   * The fragment's ListView/GridView.
   */
  private AbsListView mListView;

  /**
   * The Adapter which will be used to populate the ListView/GridView with
   * Views.
   */
  private ListAdapter mAdapter;

  public static MemoryYearFragment newInstance(MemoryAppInterface appInterface) {
    MemoryYearFragment fragment = new MemoryYearFragment();
    fragment.mApp = appInterface;
    return fragment;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public MemoryYearFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mYearsWithMemories = buildYearList();

    mAdapter = new ArrayAdapter<Integer>(getActivity(),
        android.R.layout.simple_list_item_1, android.R.id.text1, mYearsWithMemories);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_memoryyear, container, false);

    // Set the adapter
    mListView = (AbsListView) view.findViewById(android.R.id.list);
    ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

    // Set OnItemClickListener so we can be notified on item clicks
    mListView.setOnItemClickListener(this);

    return view;
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

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * Build a list of the years that have at least one memory.
   * @return a sorted List<Integer>
   */
  List<Integer> buildYearList() {
    ArrayList<Integer> retVal = new ArrayList<>();
    if (mApp != null) {
      Set<Integer> uniqueYears = mApp.getMemories().getYears();
      for (Integer year : uniqueYears) {
        retVal.add(year);
      }
      Collections.sort(retVal);
    }
    return retVal;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.
      mListener.onYearSelected(mYearsWithMemories.get(position));
    }
  }

  /**
   * The default content for this Fragment has a TextView that is shown when
   * the list is empty. If you would like to change the text, call this method
   * to supply the text it should use.
   */
  public void setEmptyText(CharSequence emptyText) {
    View emptyView = mListView.getEmptyView();

    if (emptyView instanceof TextView) {
      ((TextView) emptyView).setText(emptyText);
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
    public void onYearSelected(int year);
  }

}
