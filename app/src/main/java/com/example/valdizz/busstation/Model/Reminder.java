package com.example.valdizz.busstation.Model;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Receivers.ReminderReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Reminder implements Parcelable {

    private Station station;
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

    public void setStation(Station station) {
        this.station = station;
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

    protected Reminder(Parcel in) {
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

    public void add(Context context, AlarmManager alarmManager){
        if (periodicity!=null && periodicity.length()>0){
            for (char ch : periodicity.toCharArray()){
                setAlarm(context, alarmManager, Character.getNumericValue(ch));
            }
        }
        else {
            setAlarm(context, alarmManager, -1);
        }
    }

    public void remove(){

    }

    private void setAlarm(Context context, AlarmManager alarmManager, int periodicity){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Reminder.class.getCanonicalName(), this);
        Intent intentReminderReceiver = new Intent(context, ReminderReceiver.class);
        intentReminderReceiver.setAction(String.valueOf(getCalendar(date, time).getTimeInMillis()));
        intentReminderReceiver.putExtra(Reminder.class.getCanonicalName(), bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentReminderReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
        if (periodicity != -1) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getNextReminderDatetime(date, time, periodicity).getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            Log.d(DatabaseAccess.TAG_LOG, String.valueOf(getNextReminderDatetime(date, time, periodicity).getTimeInMillis()) + " / " + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(getNextReminderDatetime(date, time, periodicity).getTime()));
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, getCalendar(date, time).getTimeInMillis(), pendingIntent);
            Log.d(DatabaseAccess.TAG_LOG, String.valueOf(getCalendar(date, time).getTimeInMillis()) + " / " + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(getCalendar(date, time).getTime()));
        }
    }

    public Calendar getCalendar(String date, String time) {
        Calendar calendar = Calendar.getInstance();
        if (date.length() == 10 && time.length() == 5) {
            calendar.set(Calendar.YEAR, Integer.valueOf(date.substring(6)));
            calendar.set(Calendar.MONTH, Integer.valueOf(date.substring(3, 5)));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(0, 2)));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time.substring(3)));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar;
    }

    public Calendar getNextReminderDatetime(String date, String time, int periodicity){
        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.setTimeInMillis(getCalendar(date, time).getTimeInMillis());
        newReminderTime.set(Calendar.DAY_OF_WEEK, periodicity);
        if (newReminderTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            newReminderTime.add(Calendar.DAY_OF_MONTH, 7);
        }
        return newReminderTime;
    }

    public void addToDB(final DatabaseAccess databaseAccess){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.addReminder(String.valueOf(station.getId()), date, time, periodicity, note);
            }
        });
        thread.start();
    }

    public void removeFromDB(){

    }
}
