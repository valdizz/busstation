package com.valdizz.busstation.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Route implements Parcelable{

    private String number;
    private String name;
    private String color;
    private boolean direction;

    public Route(){

    }

    public Route(String number, String name, String color, boolean direction) {
        this.number = number;
        this.name = name;
        this.color = color;
        this.direction = direction;
    }

    public Route(Parcel parcel){
        this.number = parcel.readString();
        this.name = parcel.readString();
        this.color = parcel.readString();
        this.direction = parcel.readByte() != 0;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public boolean isDirection() {
        return direction;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(number);
        dest.writeString(name);
        dest.writeString(color);
        dest.writeByte((byte) (direction ? 1 : 0));
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>(){
        @Override
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };
}
