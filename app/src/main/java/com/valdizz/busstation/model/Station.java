package com.valdizz.busstation.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Station implements Parcelable {

    private final int id;
    private final Route route;
    private final String name;
    private boolean favorite;
    private final String gps;

    public Station(int id, Route route, String name, boolean favorite, String gps) {
        this.id = id;
        this.route = route;
        this.name = name;
        this.favorite = favorite;
        this.gps = gps;
    }

    public Station(Parcel parcel) {
        this.id = parcel.readInt();
        this.route = parcel.readParcelable(Route.class.getClassLoader());
        this.name = parcel.readString();
        this.favorite = parcel.readByte() != 0;
        this.gps = parcel.readString();
    }

    public int getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public String getName() {
        return name;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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
