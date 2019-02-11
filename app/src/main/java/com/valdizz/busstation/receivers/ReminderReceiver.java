package com.valdizz.busstation.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;


import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.model.Reminder;
import com.valdizz.busstation.model.Station;
import com.valdizz.busstation.R;
import com.valdizz.busstation.ScheduleActivity;

import java.util.Calendar;


public class ReminderReceiver extends BroadcastReceiver {

    private final String CHANNEL_ID = "com.valdizz.busstation.notification_channel_1";
    private final long[] VIBRATION_PATTERN = new long[] {1000, 1000, 1000, 1000, 1000, 1000};



    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        Reminder reminder = intent.getBundleExtra(Reminder.class.getCanonicalName()).getParcelable(Reminder.class.getCanonicalName());

        if (reminder != null){
            if (reminder.getPeriodicity() == null || reminder.getPeriodicity().length() == 0) {
                reminder.removeFromDB(databaseAccess, String.valueOf(reminder.getStation().getId()), reminder.getDate(), reminder.getTime(), reminder.getPeriodicity());
            } else {
                reminder.setAlarm(context, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            }
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(Station.class.getCanonicalName(), reminder != null ? reminder.getStation() : null);
        Intent intentShedule = new Intent(context, ScheduleActivity.class);
        intentShedule.putExtra(Station.class.getCanonicalName(), bundle);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(nm);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder.setContentTitle(context.getString(R.string.notification_caption, reminder.getStation().getRoute().getNumber(), reminder.getStation().getRoute().getName()));
        notificationBuilder.setContentText(context.getString(R.string.notification_text, reminder.getStation().getName()));
        notificationBuilder.setSmallIcon(R.drawable.busstation_icon);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.busstation_logo));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intentShedule, PendingIntent.FLAG_CANCEL_CURRENT));
        notificationBuilder.setLights(Color.RED, 1000, 1000);
        notificationBuilder.setVibrate(VIBRATION_PATTERN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setCategory(Notification.CATEGORY_REMINDER);
        }
        Notification notification = notificationBuilder.build();
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;
        if (nm != null) {
            nm.notify(0, notification);
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_NAME = "Bus station notification channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(VIBRATION_PATTERN);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
