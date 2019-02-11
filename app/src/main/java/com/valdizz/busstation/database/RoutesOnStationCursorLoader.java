package com.valdizz.busstation.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;


public class RoutesOnStationCursorLoader extends CursorLoader {
    private final DatabaseAccess db;
    private final Bundle bundle;

    public RoutesOnStationCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
        super(context);
        this.db = db;
        this.bundle = bundle;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return db.getRoutesOnStation(bundle.getStringArray(DatabaseAccess.BUNDLE_PARAMS));
    }
}
