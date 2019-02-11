package com.valdizz.busstation.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;


public class FavoriteStationsCursorLoader extends CursorLoader {
    private final DatabaseAccess db;

    public FavoriteStationsCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return db.getFavoriteStations();
    }
}
