package com.valdizz.busstation.database;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "BusStation.db";
    private static final int DATABASE_VERSION = 9;

    DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }


}
