package com.valdizz.busstation.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;


public class FoundStationsCursorLoader extends CursorLoader {
    private final DatabaseAccess db;
    private final Bundle bundle;

    public FoundStationsCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
        super(context);
        this.db = db;
        this.bundle = bundle;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return db.getFoundStations(bundle==null ? new String[]{"%%"} :bundle.getStringArray(DatabaseAccess.BUNDLE_PARAMS));
    }
}
