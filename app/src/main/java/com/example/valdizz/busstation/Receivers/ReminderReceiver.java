package com.example.valdizz.busstation.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;


public class ReminderReceiver extends BroadcastReceiver {

    NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentShedule = new Intent(context, SheduleActivity.class);
        intentShedule.putExtra("route_num", intent.getStringExtra("route_num"));
        intentShedule.putExtra("route_name", intent.getStringExtra("route_name"));
        intentShedule.putExtra("route_color",intent.getStringExtra("route_color"));
        intentShedule.putExtra("station_name", intent.getStringExtra("station_name"));
        intentShedule.putExtra("busstation_id", intent.getStringExtra("busstation_id"));

        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(intent.getStringExtra("route_num")+" "+intent.getStringExtra("route_name"))
                .setContentText(intent.getStringExtra("station_name")+"\n"+intent.getStringExtra("reminder_datetime"))
                .setSmallIcon(R.drawable.busstation_icon)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, intentShedule, PendingIntent.FLAG_CANCEL_CURRENT))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_MAX)
                .build();


        nm.notify(0, notification);
}
}
