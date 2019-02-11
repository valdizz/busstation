package com.valdizz.busstation.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.model.Reminder;
import com.valdizz.busstation.model.Route;
import com.valdizz.busstation.model.Station;


public class ReminderBootReceiver extends BroadcastReceiver {

    private DatabaseAccess databaseAccess;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            //Set reminders after startup
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    databaseAccess = DatabaseAccess.getInstance(context);
                    databaseAccess.open();
                    Cursor reminders = databaseAccess.getReminders();
                    while (!reminders.isAfterLast()){
                        Route route = new Route(
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                                reminders.getShort(reminders.getColumnIndex(DatabaseAccess.ROUTE_DIRECTION)) != 0);
                        Station station = new Station(
                                reminders.getInt(reminders.getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                                route,
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.STATION_NAME)),
                                reminders.getShort(reminders.getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE)) != 0,
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));
                        Reminder reminder = new Reminder(
                                station,
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.REMINDER_DATE)),
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.REMINDER_TIME)),
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.REMINDER_PERIODICITY)),
                                reminders.getString(reminders.getColumnIndex(DatabaseAccess.REMINDER_NOTE)));

                        reminder.add(context);
                        reminders.moveToNext();
                    }
                }
            });
            thread.start();
        }
    }
}
