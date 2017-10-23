package com.valdizz.busstation.Model;


import android.os.Parcel;
import android.os.Parcelable;

public class Schedule implements Parcelable {

    private Station station;
    private String time;
    private boolean weekday;
    private String description;

    public Schedule(Station station, String time, boolean day, String description) {
        this.station = station;
        this.weekday = day;
        this.description = description;
        this.time = time;
    }

    public Schedule(Parcel parcel) {
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

    public static final Parcelable.Creator<Schedule> CREATOR = new Parcelable.Creator<Schedule>(){
        @Override
        public Schedule createFromParcel(Parcel source) {
            return new Schedule(source);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };
}
