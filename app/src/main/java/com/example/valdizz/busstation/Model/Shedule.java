package com.example.valdizz.busstation.Model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Shedule implements Parcelable {

    private int id;
    private Station station;
    private List<String> timeList;
    private boolean weekday;
    private String description;

    public Shedule(int id, Station station, List<String> timeList, boolean day, String description) {
        this.id = id;
        this.station = station;
        this.weekday = day;
        this.description = description;
        this.timeList = timeList;
    }

    public Shedule(Parcel parcel) {
        this.id = parcel.readInt();
        this.station = parcel.readParcelable(Station.class.getClassLoader());
        this.timeList = parcel.readArrayList(ArrayList.class.getClassLoader());
        this.weekday = parcel.readByte() != 0;
        this.description = parcel.readString();
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

    public List<String> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<String> timeList) {
        this.timeList = timeList;
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
        dest.writeInt(id);
        dest.writeParcelable(station, flags);
        dest.writeStringList(timeList);
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
