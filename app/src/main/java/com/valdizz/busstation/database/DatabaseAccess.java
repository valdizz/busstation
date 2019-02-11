package com.valdizz.busstation.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAccess {

    //DB fields
    public final static String ROUTE_NAME = "route_name";
    public final static String ROUTE_NUMBER = "route_number";
    public final static String ROUTE_COLOR = "route_color";
    public final static String ROUTE_DIRECTION = "route_direction";
    public final static String STATION_NAME = "station_name";
    private final static String BUSSTATIONS = "BusStations";
    public final static String BUSSTATION_ID = "busstation_id";
    public final static String BUSSTATION_GPS = "gps";
    public final static String BUSSTATION_FAVORITE = "favorite";
    private final static String REMINDERS = "Reminders";
    public final static String REMINDER_DATE = "date";
    public final static String REMINDER_TIME = "time";
    public final static String REMINDER_PERIODICITY = "periodicity";
    public final static String REMINDER_NOTE = "note";
    private final static String REMINDER_BUSSTATIONS_ID = "busstations_id";
    public final static String SCHEDULE_TIME = "time";
    public final static String SCHEDULE_DESCRIPTION = "description";

    public final static String BUNDLE_PARAMS = "params";
    public static final String TAG_LOG = "BUSSTATION_LOG";

    private final SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static synchronized DatabaseAccess getInstance(Context context) {
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
        Cursor cursor = database.rawQuery("SELECT _id, name AS route_name, number AS route_number, color AS route_color, direction AS route_direction FROM routes WHERE direction=0", null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getStations(String[] params) {
        Cursor cursor = database.rawQuery("SELECT Stations._id, Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes.name AS route_name, Routes.color AS route_color FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE Routes.number=? AND Routes.direction=? ORDER BY BusStations.num_station", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTransferRoutes(String[] params) {
        Cursor cursor = database.rawQuery("SELECT  Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes._id AS route_id, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color, Routes.direction AS route_direction FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE Stations.name=? AND Routes.number<>? ORDER BY Routes._id, Routes.direction", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTransferRoutesNumbers(String[] params) {
        Cursor cursor = database.rawQuery("SELECT DISTINCT Routes.number AS route_number, Routes.color AS route_color FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE Stations.name=? AND Routes.number<>? ORDER BY Routes._id", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getSchedule(String[] params) {
        Cursor cursor = database.rawQuery("SELECT * FROM BusShedule WHERE busstations_id=? AND day=? ORDER BY time", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getFoundStations(String[] params) {
        Cursor cursor = database.rawQuery("SELECT Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes._id AS route_id, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color, Routes.direction AS route_direction FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE station_name LIKE ? ORDER BY route_id, BusStations.num_station", params);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getFavoriteStations() {
        Cursor cursor = database.rawQuery("SELECT Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes._id AS route_id, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color, Routes.direction AS route_direction FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE BusStations.favorite=1 ORDER BY route_id, BusStations.num_station", null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getAllStations() {
        Cursor cursor = database.rawQuery("SELECT Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) ORDER BY route_number, BusStations.num_station", null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRoutesOnStation(String[] params) {
        Cursor cursor = database.rawQuery("SELECT Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Routes._id AS route_id, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color, Routes.direction AS route_direction FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) WHERE gps = ? ORDER BY route_id, BusStations.num_station", params);
        cursor.moveToFirst();
        return cursor;
    }

    public void setFavoriteStation(String favorite, String busstations_id) {
        ContentValues values = new ContentValues();
        values.put(BUSSTATION_FAVORITE, favorite);
        database.update(BUSSTATIONS, values, "_id=?", new String[]{busstations_id});
    }

    public Cursor getReminders() {
        Cursor cursor = database.rawQuery("SELECT Stations.name AS station_name, BusStations._id AS busstation_id, BusStations.*, Reminders.*, Routes._id AS route_id, Routes.number AS route_number, Routes.name AS route_name, Routes.color AS route_color, Routes.direction AS route_direction FROM BusStations INNER JOIN Stations ON (BusStations.stations_id = Stations._id) INNER JOIN Routes ON (BusStations.routes_id = Routes._id) INNER JOIN Reminders ON (BusStations._id = Reminders.busstations_id) ORDER BY route_id, BusStations.num_station", null);
        cursor.moveToFirst();
        return cursor;
    }

    public void addReminder(String busstations_id, String date, String time, String periodicity, String note) {
        ContentValues values = new ContentValues();
        values.put(REMINDER_BUSSTATIONS_ID, busstations_id);
        values.put(REMINDER_DATE, date);
        values.put(REMINDER_TIME, time);
        values.put(REMINDER_PERIODICITY, periodicity);
        values.put(REMINDER_NOTE, note);
        database.beginTransaction();
        try{
            database.delete(REMINDERS, "busstations_id =? AND date=? AND time=? AND periodicity=?", new String[]{busstations_id, date, time, periodicity});
            database.insert(REMINDERS, null, values);
            database.setTransactionSuccessful();
        }
        finally {
            database.endTransaction();
        }
    }

    public void deleteReminder(String id){
        database.delete(REMINDERS, "_id=?", new String[]{id});
    }

    public void deleteReminder(String busstations_id, String date, String time, String periodicity){
        database.delete(REMINDERS, "busstations_id =? AND date=? AND time=? AND periodicity=?", new String[]{busstations_id, date, time, periodicity});
    }


}
