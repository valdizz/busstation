package com.valdizz.busstation.Database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;


public class RoutesCursorLoader extends CursorLoader {
    DatabaseAccess db;

    public RoutesCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = db.getRoutes();
        return cursor;
    }
}
