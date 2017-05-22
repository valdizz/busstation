package com.example.valdizz.busstation.Database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

/**
 * Created by V.Kornev on 22.05.17.
 */

public class FavoriteStationsCursorLoader extends CursorLoader {
    DatabaseAccess db;

    public FavoriteStationsCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = db.getFavoriteStations();
        return cursor;
    }
}
