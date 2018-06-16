package net.ghosttrails.www.mydetic;

import android.app.Activity;
import android.os.SystemClock;

/** A Base class for activities that lock themselves with the security PIN fragment when resumed. */
public abstract class LockableActivity extends Activity
    implements SecurityPinFragment.OnFragmentInteractionListener {

  // If we move from one LockableActivity to another in less than this amount, then
  // the screen isn't locked.
  public static long PIN_LOCK_DELAY_MS = 1000;

  private SecurityPinFragment pinFragment;

  @Override
  protected void onResume() {
    super.onResume();

    MemoryAppState appState = MemoryAppState.getInstance();

    long delayMillis = SystemClock.elapsedRealtime() - getTimePaused();
    // If the delay is negative, this may indicate a device reboot since the app was last
    // paused.
    boolean resumedQuickly = delayMillis > 0L && delayMillis < PIN_LOCK_DELAY_MS;
    if (appState.getConfig().isUsingSecurityPin() && (!resumedQuickly || isPinLockDisplayed())) {
      // Add the PIN fragment
      if (pinFragment == null) {
        pinFragment = new SecurityPinFragment();
      }
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.pin_fragment_container, pinFragment)
          .commit();
      setPinLockDisplayed(true);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    setTimePaused();
  }

  @Override
  public void onDismissed() {
    if (pinFragment != null) {
      getFragmentManager().beginTransaction().remove(pinFragment).commit();
    }
    setPinLockDisplayed(false);
    pinFragment = null;
  }

  public boolean isPinLockDisplayed() {
    return getSharedPreferences("MyDeticTransition", 0).getBoolean("IS_PIN_LOCK_DISPLAYED", false);
  }

  public void setPinLockDisplayed(boolean isDisplayed) {
    getSharedPreferences("MyDeticTransition", 0)
        .edit()
        .putBoolean("IS_PIN_LOCK_DISPLAYED", isDisplayed)
        .apply();
  }

  /**
   * Save a timestamp when a LockableActivity is paused. The pin pad won't be displayed if the next
   * LockableActivity created is within PIN_LOCK_DELAY_MS.
   */
  public void setTimePaused() {
    getSharedPreferences("MyDeticTransition", 0)
        .edit()
        .putLong("PAUSE_TIME_MS", SystemClock.elapsedRealtime())
        .apply();
  }

  public long getTimePaused() {
    return getSharedPreferences("MyDeticTransition", 0).getLong("PAUSE_TIME_MS", 1L);
  }
}
