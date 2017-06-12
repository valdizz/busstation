package com.example.valdizz.busstation.Model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class Reminder implements Parcelable{

    private int id;
    private Station station;
    private Calendar datetime;
    private String periodicity;
    private String note;

    public Reminder(int id, Station station, Calendar datetime, String periodicity, String note) {
        this.id = id;
        this.station = station;
        this.datetime = datetime;
        this.periodicity = periodicity;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Calendar getDatetime() {
        return datetime;
    }

    public void setDatetime(Calendar datetime) {
        this.datetime = datetime;
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
        dest.writeInt(this.id);
        dest.writeParcelable(this.station, flags);
        dest.writeSerializable(this.datetime);
        dest.writeString(this.periodicity);
        dest.writeString(this.note);
    }

    protected Reminder(Parcel in) {
        this.id = in.readInt();
        this.station = in.readParcelable(Station.class.getClassLoader());
        this.datetime = (Calendar) in.readSerializable();
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
}
