package com.example.sleepaid.Service.BlueLightFilter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.time.ZonedDateTime;

public class BlueLightFilterBroadcastReceiverService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, BlueLightFilterService.class);

        if (intent.getAction() != null && intent.getAction().equals("STOP")) {
            BlueLightFilterService.setIsRunning(false);
            context.stopService(serviceIntent);
            return;
        }

        this.scheduleNext(context, intent);
        this.scheduleStop(context);

        context.startForegroundService(serviceIntent);
    }

    private void scheduleNext(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent newStartIntent = new Intent(context, BlueLightFilterBroadcastReceiverService.class);

        int startHour = intent.getIntExtra("HOUR", 20);
        int startMinute = intent.getIntExtra("MINUTE", 0);

        long startTime = ZonedDateTime.now()
                .withHour(startHour)
                .withMinute(startMinute)
                .plusDays(1)
                .toInstant()
                .toEpochMilli();

        newStartIntent.putExtra("HOUR", startHour);
        newStartIntent.putExtra("MINUTE", startMinute);

        PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, (int) startTime, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setAlarmClock(
                new AlarmManager.AlarmClockInfo(startTime, startPendingIntent),
                startPendingIntent
        );
    }

    private void scheduleStop(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent stopIntent = new Intent(context, BlueLightFilterBroadcastReceiverService.class);

        long stopTime = ZonedDateTime.now()
                .withHour(7)
                .withMinute(30)
                .plusDays(1)
                .toInstant()
                .toEpochMilli();

        stopIntent.setAction("STOP");

        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, (int) stopTime, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setAlarmClock(
                new AlarmManager.AlarmClockInfo(stopTime, stopPendingIntent),
                stopPendingIntent
        );
    }
}