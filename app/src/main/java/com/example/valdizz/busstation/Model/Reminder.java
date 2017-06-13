package com.example.valdizz.busstation.Model;


import android.os.Parcel;
import android.os.Parcelable;

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

    public String getReminderTitle(){
        return "Route: " + station.getRoute().getNumber() + "  " + station.getRoute().getName();
    }

    public String getReminderText(){
        if (periodicity!=null && periodicity.length() > 0) {
            StringBuilder days = new StringBuilder();
            for (char ch : periodicity.toCharArray()){
                switch (Character.getNumericValue(ch)){
                    case (Calendar.MONDAY):{
                        days.append("Mon");
                        break;
                    }
                    case (Calendar.TUESDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Tue");
                        break;
                    }
                    case (Calendar.WEDNESDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Wed");
                        break;
                    }
                    case (Calendar.THURSDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Thu");
                        break;
                    }
                    case (Calendar.FRIDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Fri");
                        break;
                    }
                    case (Calendar.SATURDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Sat");
                        break;
                    }
                    case (Calendar.SUNDAY):{
                        days.append(days.length()!=0 ? ", ": "").append("Sun");
                        break;
                    }
                }
            }
            return "Station: " + station.getName() + "\n" + "Start: " + days.toString() + " at " + time;
        }
        else {
            return "Station: " + station.getName() + "\n" + "Start: " + date + " at " + time;
        }
    }
}
