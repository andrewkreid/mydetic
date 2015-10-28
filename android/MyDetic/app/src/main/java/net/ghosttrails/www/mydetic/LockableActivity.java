package net.ghosttrails.www.mydetic;

import android.app.Activity;

/**
 * A Base class for activities that lock themselves with the security PIN fragment when resumed.
 */
public class LockableActivity extends Activity
    implements SecurityPinFragment.OnFragmentInteractionListener {

  private SecurityPinFragment pinFragment;

  @Override
  protected void onResume() {
    super.onResume();

    if (!isTransitioningToAppActivity() || isPinLockDisplayed()) {
      // Add the PIN fragment
      if (pinFragment == null) {
        pinFragment = new SecurityPinFragment();
      }
      getFragmentManager().beginTransaction()
          .replace(R.id.pin_fragment_container, pinFragment).commit();
      setPinLockDisplayed(true);
    }
    setTransitioningToAppActivity(false);
  }

  @Override
  public void onDismissed() {
    if (pinFragment != null) {
      getFragmentManager().beginTransaction().remove(pinFragment).commit();
    }
    setPinLockDisplayed(false);
    pinFragment = null;
  }

  public void setPinLockDisplayed(boolean isDisplayed) {
    getSharedPreferences("MyDeticTransition", 0).edit()
        .putBoolean("IS_PIN_LOCK_DISPLAYED", isDisplayed).commit();
  }

  public boolean isPinLockDisplayed() {
    return getSharedPreferences("MyDeticTransition", 0).getBoolean("IS_PIN_LOCK_DISPLAYED", false);
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

}
