package net.ghosttrails.www.mydetic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Class for receiving a notification on device boot. Is responsible for restoring the daily
 * reminder alarm.
 */
public class BootWakeupReceiver extends WakefulBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    AppUtils.setReminderNotification(context);
  }
}
