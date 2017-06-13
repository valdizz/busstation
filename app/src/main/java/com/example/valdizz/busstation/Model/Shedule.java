package com.example.valdizz.busstation.Model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Shedule implements Parcelable {

    private Station station;
    private String time;
    private boolean weekday;
    private String description;

    public Shedule(Station station, String time, boolean day, String description) {
        this.station = station;
        this.weekday = day;
        this.description = description;
        this.time = time;
    }

    public Shedule(Parcel parcel) {
        this.station = parcel.readParcelable(Station.class.getClassLoader());
        this.time = parcel.readString();
        this.weekday = parcel.readByte() != 0;
        this.description = parcel.readString();
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isWeekday() {
        return weekday;
    }

    public void setWeekday(boolean weekday) {
        this.weekday = weekday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(station, flags);
        dest.writeString(time);
        dest.writeByte((byte) (weekday ? 1 : 0));
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Shedule> CREATOR = new Parcelable.Creator<Shedule>(){
        @Override
        public Shedule createFromParcel(Parcel source) {
            return new Shedule(source);
        }

        @Override
        public Shedule[] newArray(int size) {
            return new Shedule[size];
        }
    };
}
