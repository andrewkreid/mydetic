package net.ghosttrails.www.mydetic;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Date picker fragment
 */
public class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

  private List<DatePickerDialog.OnDateSetListener> listeners;

  public DatePickerFragment() {
    super();
    listeners = new ArrayList<DatePickerDialog.OnDateSetListener>();
  }

  public void addListener(DatePickerDialog.OnDateSetListener listener) {
    listeners.add(listener);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the current time as the default values for the picker
    final Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);

    // Create a new instance of DatePickerDialog and return it
    return new DatePickerDialog(getActivity(), this, year, month, day);
  }

  @Override
  public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    for(DatePickerDialog.OnDateSetListener listener: listeners) {
      listener.onDateSet(datePicker, year, month, day);
    }
  }
}