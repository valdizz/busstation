package com.example.valdizz.busstation.Database;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;


public class StationsCursorLoader extends CursorLoader{
    DatabaseAccess db;
    Bundle bundle;

    public StationsCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
        super(context);
        this.db = db;
        this.bundle = bundle;
    }

    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = db.getStations(bundle.getStringArray(DatabaseAccess.BUNDLE_PARAMS));
        return cursor;
    }
}
