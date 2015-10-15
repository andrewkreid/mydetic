package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SecurityPinFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SecurityPinFragment extends Fragment implements View.OnClickListener {

  private OnFragmentInteractionListener mListener;

  private Button dismissButton;

  public SecurityPinFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_security_pin, container, false);

    dismissButton = (Button) view.findViewById(R.id.pin_dismiss_button);
    dismissButton.setOnClickListener(this);

    return view;
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onDismissButtonPressed() {
    if (mListener != null) {
      mListener.onDismissed();
    }
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

  @Override
  public void onClick(View v) {
    switch(v.getId()) {
      case R.id.pin_dismiss_button:
        mListener.onDismissed();
        break;
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

    /**
     * Notification to close the fragment.
     */
    void onDismissed();
  }

}
