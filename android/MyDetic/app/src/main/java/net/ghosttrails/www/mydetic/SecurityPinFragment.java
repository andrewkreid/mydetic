package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SecurityPinFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SecurityPinFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private TextView pinDigitViews[];
    private Integer pinDigits[];

    // Which pin digit is the next one typed.
    private int pinEntryIdx;

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

        pinEntryIdx = 0;
        pinDigits = new Integer[4];
        Arrays.fill(pinDigits, -1);
        pinDigitViews = new TextView[4];
        pinDigitViews[0] = (TextView) view.findViewById(R.id.pin_digit_1);
        pinDigitViews[1] = (TextView) view.findViewById(R.id.pin_digit_2);
        pinDigitViews[2] = (TextView) view.findViewById(R.id.pin_digit_3);
        pinDigitViews[3] = (TextView) view.findViewById(R.id.pin_digit_4);

        int[] numberButtonIds = {R.id.pin_button_0,
                R.id.pin_button_1,
                R.id.pin_button_2,
                R.id.pin_button_3,
                R.id.pin_button_4,
                R.id.pin_button_5,
                R.id.pin_button_6,
                R.id.pin_button_7,
                R.id.pin_button_8,
                R.id.pin_button_9,
                R.id.pin_button_clear,
        };
        for (int btnId : numberButtonIds) {
            Button numberButton = (Button) view.findViewById(btnId);
            numberButton.setOnClickListener(this);
        }

        pinDigitViews[0].requestFocus();
        updateDigitDisplay();

        return view;
    }

    @Override
    public void onAttach(Context activity) {
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
        switch (v.getId()) {
            case R.id.pin_button_0:
                typedANumber(0);
                break;
            case R.id.pin_button_1:
                typedANumber(1);
                break;
            case R.id.pin_button_2:
                typedANumber(2);
                break;
            case R.id.pin_button_3:
                typedANumber(3);
                break;
            case R.id.pin_button_4:
                typedANumber(4);
                break;
            case R.id.pin_button_5:
                typedANumber(5);
                break;
            case R.id.pin_button_6:
                typedANumber(6);
                break;
            case R.id.pin_button_7:
                typedANumber(7);
                break;
            case R.id.pin_button_8:
                typedANumber(8);
                break;
            case R.id.pin_button_9:
                typedANumber(9);
                break;
            case R.id.pin_button_clear:
                if (pinEntryIdx >= 1) {
                    pinEntryIdx--;
                    pinDigits[pinEntryIdx] = -1;
                    updateDigitDisplay();
                }
        }
    }

    private void typedANumber(int digit) {
        //Log.e("MyDeticPinPad", String.format("typedANumber IN (%d) to idx %d", digit, pinEntryIdx));
        if (pinEntryIdx < 4) {
            pinDigits[pinEntryIdx] = digit;
            pinEntryIdx++;
            updateDigitDisplay();
        }

        // On the last number? see if it's right.
        if (pinEntryIdx == 4) {
            MemoryAppState appState = MemoryAppState.getInstance();
            MyDeticConfig config = appState.getConfig();
            String enteredPin = String.format(Locale.getDefault(),
                    "%d%d%d%d", pinDigits[0], pinDigits[1], pinDigits[2], pinDigits[3]);
            if (enteredPin.equals(config.getSecurityPin())) {
                mListener.onDismissed();
            } else {
                // Clear and reset.
                Arrays.fill(pinDigits, -1);
                pinEntryIdx = 0;
                updateDigitDisplay();
                // TODO : provide to option to unlock at the cost of clearing cache and resetting
                // TODO : config password.
            }
        }
        //Log.e("MyDeticPinPad", String.format("typedANumber OUT (%d) to idx %d", digit, pinEntryIdx));
    }

    private void updateDigitDisplay() {
        for (int i = 0; i < 4; i++) {
            if (pinDigits[i] != -1) {
                pinDigitViews[i].setText("*");
            } else {
                pinDigitViews[i].setText("");
            }
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
