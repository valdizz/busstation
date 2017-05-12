package com.example.valdizz.busstation;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAccess {

    public final static String DIRECTION_UP = "0";
    public final static String DIRECTION_DOWN = "1";
    public final static String SHEDULE_WORKDAY = "0";
    public final static String SHEDULE_WEEKDAY = "1";


    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    public Cursor getRoutes() {
        Cursor cursor = database.rawQuery("SELECT * FROM routes WHERE direction=0", null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getStations(String[] params) {
        Cursor cursor = database.rawQuery("SELECT Stations.*, BusStations.*, Routes.name AS route_name, Routes.color AS route_color FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE Routes.number=? AND Routes.direction=? ORDER BY BusStations.num_station", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getShedule(String[] params) {
        Cursor cursor = database.rawQuery("SELECT * FROM BusShedule WHERE busstations_id=? AND time LIKE ? AND day=? ORDER BY time", params);
        cursor.moveToFirst();
        return cursor;
    }
}
