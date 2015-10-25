package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;

import java.util.List;

/**
 * A Base class for activities that lock themselves with the security PIN fragment when resumed.
 */
public class LockableActivity extends Activity
    implements SecurityPinFragment.OnFragmentInteractionListener {

  private SecurityPinFragment pinFragment;

  @Override
  protected void onResume() {
    super.onResume();

    if (!isTransitioningToAppActivity()) {
      // Add the PIN fragment
      if (pinFragment == null) {
        pinFragment = new SecurityPinFragment();
      }
      getFragmentManager().beginTransaction()
          .replace(R.id.pin_fragment_container, pinFragment).commit();
    }
    setTransitioningToAppActivity(false);
  }

  @Override
  public void onDismissed() {
    if (pinFragment != null) {
      getFragmentManager().beginTransaction().remove(pinFragment).commit();
    }
    pinFragment = null;
  }

  /**
   * Set a flag when moving from one MyDetic activity to another so that the PIN pad doesn't
   * come up in onResume()
   */
  public void setTransitioningToAppActivity(boolean isTransitioning) {
    getSharedPreferences("MyDeticTransition", 0).edit()
        .putBoolean("IS_TRANSITIONING", isTransitioning).commit();
  }

  public boolean isTransitioningToAppActivity() {
    return getSharedPreferences("MyDeticTransition", 0).getBoolean("IS_TRANSITIONING", false);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // Called when the device is rotated. Since the activity will be recreated, set the
    // transition flag so we don't get the pin pad.
    // Also need to set android:configChanges="orientation|screenSize" in the manifest for this
    // to get called.
    setTransitioningToAppActivity(true);
    super.onConfigurationChanged(newConfig);
  }
}
