package com.example.sleepaid.Service.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NotificationBroadcastReceiverService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startRescheduleNotificationsService(context);
        }
        else {
            startNotificationService(context, intent);

            if (intent.getBooleanExtra("DAILY", false)) {
                startRepeatNotificationService(intent);
            }
        }
    }

    private void startNotificationService(Context context, Intent intent) {
        Intent intentService = new Intent(context, NotificationService.class);

        intentService.putExtra("ID", intent.getIntExtra("ID", 0));
        intentService.putExtra("NAME", intent.getStringExtra("NAME"));
        intentService.putExtra("CONTENT", intent.getStringExtra("CONTENT"));
        intentService.putExtra("DESTINATION", intent.getIntExtra("DESTINATION", 0));

        context.startForegroundService(intentService);
    }

    private void startRepeatNotificationService(Intent intent) {
        RepeatNotificationService repeatNotificationService = new RepeatNotificationService();
        repeatNotificationService.scheduleRepeat(intent.getIntExtra("ID", 0));
    }

    private void startRescheduleNotificationsService(Context context) {
        Intent intentService = new Intent(context, RescheduleNotificationsService.class);
        context.startForegroundService(intentService);
    }
}
