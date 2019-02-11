package com.valdizz.busstation.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;


public class RoutesCursorLoader extends CursorLoader {
    private final DatabaseAccess db;

    public RoutesCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return db.getRoutes();
    }
}
