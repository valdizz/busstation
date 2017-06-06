package com.example.valdizz.busstation.Database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;


public class RemindersCursorLoader extends CursorLoader {
    DatabaseAccess db;

    public RemindersCursorLoader(Context context, DatabaseAccess db) {
        super(context);
        this.db = db;
    }

    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = db.getReminders();
        return cursor;
    }
}
