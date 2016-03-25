package net.ghosttrails.www.mydetic;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives timed alarms (set in SettingsActivity) for the reminder to enter your daily memory,
 * and posts a notification to that effect.
 */
public class AlarmReceiver extends BroadcastReceiver {

  public static String NOTIFICATION_ID = "notification-id";
  public static String NOTIFICATION = "notification";

  public void onReceive(Context context, Intent intent) {

    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    Notification notification = intent.getParcelableExtra(NOTIFICATION);
    int id = intent.getIntExtra(NOTIFICATION_ID, 0);
    if (notification != null) {
      notificationManager.notify(id, notification);
    }
  }

}