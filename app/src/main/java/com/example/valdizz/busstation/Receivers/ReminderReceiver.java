package com.example.valdizz.busstation.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.example.valdizz.busstation.Model.Reminder;
import com.example.valdizz.busstation.Model.Shedule;
import com.example.valdizz.busstation.Model.Station;
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;


public class ReminderReceiver extends BroadcastReceiver {

    NotificationManager nm;
    Reminder reminder;

    @Override
    public void onReceive(Context context, Intent intent) {
        reminder = intent.getParcelableExtra(Reminder.class.getCanonicalName());
        Intent intentShedule = new Intent(context, SheduleActivity.class);
        intentShedule.putExtra(Station.class.getCanonicalName(), reminder.getStation());

        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(reminder.getReminderTitle())
                .setContentText(reminder.getReminderText())
                .setSmallIcon(R.drawable.busstation_icon)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, intentShedule, PendingIntent.FLAG_CANCEL_CURRENT))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        nm.notify(0, notification);
}
}
