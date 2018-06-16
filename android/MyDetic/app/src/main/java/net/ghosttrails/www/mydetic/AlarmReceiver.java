package net.ghosttrails.www.mydetic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

/**
 * Receives timed alarms (set in SettingsActivity) for the reminder to enter your daily memory, and
 * posts a notification to that effect.
 */
public class AlarmReceiver extends BroadcastReceiver {

  public static String NOTIFICATION_ID = "notification-id";
  public static String NOTIFICATION = "notification";

  public static String NOTIFICATION_CHANNEL_ID = "mydetic_channel_01";

  public void onReceive(Context context, Intent intent) {

    createNotificationChannel(context);
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      Notification notification = intent.getParcelableExtra(NOTIFICATION);
      int id = intent.getIntExtra(NOTIFICATION_ID, 0);
      if (notification != null) {
        notificationManager.notify(id, notification);
      }
    }
  }

  /**
   * Creates the notification channel. Ok to do this each time as its a no-op if the channel already
   * exists.
   *
   * <p>This is new for Android Oreo.
   */
  private void createNotificationChannel(Context context) {
    Context appContext = context.getApplicationContext();
    NotificationManager mNotificationManager =
        (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
    if (mNotificationManager == null) {
      AppUtils.smallToast(context, "Could not fetch notification service");
      return;
    }
    // The id of the channel.
    // The user-visible name of the channel.
    CharSequence name = appContext.getString(R.string.channel_name);
    // The user-visible description of the channel.
    String description = appContext.getString(R.string.channel_description);
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationChannel mChannel =
        new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
    // Configure the notification channel.
    mChannel.setDescription(description);
    mChannel.enableLights(true);
    // Sets the notification light color for notifications posted to this
    // channel, if the device supports this feature.
    mChannel.setLightColor(Color.RED);
    mChannel.enableVibration(true);
    mChannel.setVibrationPattern(new long[] {100, 200, 300, 400, 500, 400, 300, 200, 400});
    mNotificationManager.createNotificationChannel(mChannel);
  }
}
