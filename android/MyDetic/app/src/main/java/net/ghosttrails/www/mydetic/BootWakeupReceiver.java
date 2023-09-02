package net.ghosttrails.www.mydetic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Class for receiving a notification on device boot. Is responsible for restoring the daily
 * reminder alarm.
 */
public class BootWakeupReceiver extends BroadcastReceiver {

  public static class NotifyWorker extends Worker {
    public NotifyWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
      super(context, params);
    }

    @Override
    public Result doWork() {
      AppUtils.setReminderNotification(getApplicationContext());
      return Result.success();
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {

    WorkRequest uploadWorkRequest =
            new OneTimeWorkRequest.Builder(NotifyWorker.class)
                    .build();

    WorkManager
            .getInstance(context)
            .enqueue(uploadWorkRequest);

  }
}
