package net.ghosttrails.www.mydetic;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import androidx.core.app.TaskStackBuilder;
import java.util.Calendar;

/** Miscellaneous helper functions. */
class AppUtils {

  static void smallToast(Context context, String msg) {
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(context.getApplicationContext(), msg, duration);
    toast.show();
  }

  static void setReminderNotification(Context context) {

    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    // Set the alarm to start at approximately 2105
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.set(Calendar.HOUR_OF_DAY, 21);
    calendar.set(Calendar.MINUTE, 5);

    PendingIntent alarmIntent = createAlarmIntent(context);

    // With setInexactRepeating(), you have to use one of the AlarmManager interval
    // constants--in this case, AlarmManager.INTERVAL_DAY.
    alarmMgr.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.getTimeInMillis(),
        AlarmManager.INTERVAL_DAY,
        alarmIntent);

    // Set notification for 10 seconds in the future - for testing.
//    long futureInMillis = SystemClock.elapsedRealtime() + 10000;
//    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, alarmIntent);

  }

  static void cancelReminderNotification(Context context) {
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    PendingIntent alarmIntent = createAlarmIntent(context);
    if (alarmMgr != null) {
      alarmMgr.cancel(alarmIntent);
    }
  }

  private static PendingIntent createAlarmIntent(Context context) {
    PendingIntent alarmIntent;

    // Tapping on the notification just goes to the home screen.
    // TODO: Maybe go straight to MemoryDetailActivity for the current day?
    Intent resultIntent = new Intent(context, HomeActivity.class);

    // This StackBuilder stuff is supposed to make the back button behavior work as expected.
    // Since we're intenting into the home screen I'm not sure it's necessary.
    // See
    // http://developer.android.com/guide/topics/ui/notifiers/notifications.html#NotificationResponse
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    // Adds the back stack
    stackBuilder.addParentStack(HomeActivity.class);
    // Adds the Intent to the top of the stack
    stackBuilder.addNextIntent(resultIntent);
    // Gets a PendingIntent containing the entire back stack
    PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);

    Intent intent = new Intent(context, AlarmReceiver.class);
    Notification notification =
        new Notification.Builder(context, AlarmReceiver.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.mydetic_notification_white_24dp)
            .setContentTitle("MyDetic reminder")
            .setContentText("Enter today's memory")
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.mydetic))
            .build();

    intent.putExtra(AlarmReceiver.NOTIFICATION_ID, 1);
    intent.putExtra(AlarmReceiver.NOTIFICATION, notification);
    alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

    return alarmIntent;
  }
}
