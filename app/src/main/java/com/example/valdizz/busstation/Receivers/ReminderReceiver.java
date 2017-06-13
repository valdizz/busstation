package com.example.valdizz.busstation.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.example.valdizz.busstation.MainActivity;
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
        reminder = intent.getBundleExtra(Reminder.class.getCanonicalName()).getParcelable(Reminder.class.getCanonicalName());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Station.class.getCanonicalName(), reminder.getStation());
        Intent intentShedule = new Intent(context, SheduleActivity.class);
        intentShedule.putExtra(Station.class.getCanonicalName(), bundle);

        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notification_caption, reminder.getStation().getRoute().getNumber(), reminder.getStation().getRoute().getName()))
                .setContentText(context.getString(R.string.notification_text, reminder.getStation().getName()))
                .setSmallIcon(R.drawable.busstation_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.busstation_logo))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, intentShedule, PendingIntent.FLAG_CANCEL_CURRENT))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] {1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 1000, 1000)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_REMINDER)
                .build();
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;

        nm.notify(0, notification);
}
}
