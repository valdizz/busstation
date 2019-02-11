package com.valdizz.busstation.model;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.receivers.ReminderReceiver;

import java.util.Calendar;

public class Reminder implements Parcelable {

    private final Station station;
    private String date;
    private String time;
    private String periodicity;
    private String note;

    public Reminder(Station station, String date, String time, String periodicity, String note) {
        this.station = station;
        this.date = date;
        this.time = time;
        this.periodicity = periodicity;
        this.note = note;
    }

    public Station getStation() {
        return station;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.station, flags);
        dest.writeString(this.date);
        dest.writeString(this.time);
        dest.writeString(this.periodicity);
        dest.writeString(this.note);
    }

    private Reminder(Parcel in) {
        this.station = in.readParcelable(Station.class.getClassLoader());
        this.date = in.readString();
        this.time = in.readString();
        this.periodicity = in.readString();
        this.note = in.readString();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public void add(Context context) {
        if (periodicity != null && periodicity.length() > 0) {
            for (char ch : periodicity.toCharArray()) {
                setAlarm(context, Character.getNumericValue(ch));
            }
        } else {
            setAlarm(context, -1);
        }
    }

    public void remove(Context context) {
        if (periodicity != null && periodicity.length() > 0) {
            for (char ch : periodicity.toCharArray()) {
                removeAlarm(context, Character.getNumericValue(ch));
            }
        } else {
            removeAlarm(context, -1);
        }
    }

    public void setAlarm(Context context, int periodicity) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Reminder.class.getCanonicalName(), this);
        Intent intentReminderReceiver = new Intent(context, ReminderReceiver.class);
        intentReminderReceiver.setAction(String.valueOf(getNextReminderDatetime(date, time, periodicity).getTimeInMillis()) + "_" + periodicity);
        intentReminderReceiver.putExtra(Reminder.class.getCanonicalName(), bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentReminderReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getNextReminderDatetime(date, time, periodicity).getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, getNextReminderDatetime(date, time, periodicity).getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getNextReminderDatetime(date, time, periodicity).getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void removeAlarm(Context context, int periodicity) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Reminder.class.getCanonicalName(), this);
        Intent intentReminderReceiver = new Intent(context, ReminderReceiver.class);
        intentReminderReceiver.setAction(String.valueOf(getNextReminderDatetime(date, time, periodicity).getTimeInMillis()) + "_" + periodicity);
        intentReminderReceiver.putExtra(Reminder.class.getCanonicalName(), bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentReminderReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public Calendar getNextReminderDatetime(String date, String time, int periodicity) {
        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
        newReminderTime.set(Calendar.MINUTE, Integer.valueOf(time.substring(3)));
        newReminderTime.set(Calendar.SECOND, 0);
        newReminderTime.set(Calendar.MILLISECOND, 0);
        if (periodicity != -1) {
            newReminderTime.set(Calendar.DAY_OF_WEEK, periodicity);
            if (newReminderTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                newReminderTime.add(Calendar.DAY_OF_MONTH, 7);
            }
        } else {
            newReminderTime.set(Calendar.YEAR, Integer.valueOf(date.substring(6)));
            newReminderTime.set(Calendar.MONTH, Integer.valueOf(date.substring(3, 5)));
            newReminderTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(0, 2)));
        }
        return newReminderTime;
    }

    public void addToDB(final DatabaseAccess databaseAccess) {
        Log.d(DatabaseAccess.TAG_LOG, "Add to DB: " + toString());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.addReminder(String.valueOf(station.getId()), date, time, periodicity, note);
            }
        });
        thread.start();
    }

    public void removeFromDB(final DatabaseAccess databaseAccess, final String id) {
        Log.d(DatabaseAccess.TAG_LOG, "Remove from DB: " + this.toString());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.deleteReminder(id);
            }
        });
        thread.start();
    }

    public void removeFromDB(final DatabaseAccess databaseAccess, final String busstations_id, final String date, final String time, final String periodicity) {
        Log.d(DatabaseAccess.TAG_LOG, "Remove from DB: " + this.toString());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.deleteReminder(busstations_id, date, time, periodicity);
            }
        });
        thread.start();
    }

    @NonNull
    @Override
    public String toString() {
        return "Reminder{" +
                "station=" + station.getId() +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", periodicity='" + periodicity + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
