package com.example.valdizz.busstation.Model;

import android.os.Parcel;
import android.os.Parcelable;


public class Station implements Parcelable {

    private int id;
    private Route route;
    private String name;
    private int number;
    private boolean favorite;
    private String gps;

    public Station(int id, Route route, String name, int number, boolean favorite, String gps) {
        this.id = id;
        this.route = route;
        this.name = name;
        this.number = number;
        this.favorite = favorite;
        this.gps = gps;
    }

    public Station(Parcel parcel) {
        this.id = parcel.readInt();
        this.route = parcel.readParcelable(Route.class.getClassLoader());
        this.name = parcel.readString();
        this.number = parcel.readInt();
        this.favorite = parcel.readByte() != 0;
        this.gps = parcel.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(route, flags);
        dest.writeString(name);
        dest.writeInt(number);
        dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(gps);
    }

    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>(){
        @Override
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}
