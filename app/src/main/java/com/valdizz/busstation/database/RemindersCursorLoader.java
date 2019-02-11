package com.valdizz.busstation.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;


public class RemindersCursorLoader extends CursorLoader {
    private final DatabaseAccess db;

    public RemindersCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return db.getReminders();
    }
}
