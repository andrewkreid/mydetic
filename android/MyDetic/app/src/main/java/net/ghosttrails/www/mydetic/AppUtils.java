package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.widget.Toast;

/**
 * Miscellaneous helper functions.
 */
public class AppUtils {

  public static void smallToast(Context context, String msg) {
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(context.getApplicationContext(), msg, duration);
    toast.show();
  }
}
