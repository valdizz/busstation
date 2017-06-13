package com.example.valdizz.busstation;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.RemindersCursorLoader;
import com.example.valdizz.busstation.Model.Reminder;
import com.example.valdizz.busstation.Model.Route;
import com.example.valdizz.busstation.Model.Shedule;
import com.example.valdizz.busstation.Model.Station;

public class RemindersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CM_DELETE_ID = 0;

    ListView lvReminders;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRemindersAdapter;
    Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        lvReminders = (ListView) findViewById(R.id.lvReminders);

        initializeContentLoader();
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME, DatabaseAccess.STATION_NAME, DatabaseAccess.REMINDER_DATETIME};
        int[] to = new int[]{R.id.tvRouteNumReminderItem, R.id.tvRouteNameReminderItem, R.id.tvStationNameReminderItem, R.id.tvDateTimeReminderItem};
        scRemindersAdapter = new SimpleCursorAdapter(this, R.layout.reminder_item, null, from, to, 0);
        scRemindersAdapter.setViewBinder(new RemindersAdapterViewBinder());
        lvReminders.setAdapter(scRemindersAdapter);
        lvReminders.setOnItemClickListener(reminderOnClickListener);
        registerForContextMenu(lvReminders);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private AdapterView.OnItemClickListener reminderOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Route route = new Route(
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                    scRemindersAdapter.getCursor().getShort(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION))!=0);
            Station station = new Station(
                    scRemindersAdapter.getCursor().getInt(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                    route,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                    scRemindersAdapter.getCursor().getShort(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE))!=0,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));
            reminder = new Reminder(
                    station,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_DATE)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_TIME)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_PERIODICITY)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_NOTE)));

            Intent intentRemiderSettings = new Intent(RemindersActivity.this, ReminderSettingsActivity.class);
            intentRemiderSettings.putExtra(Reminder.class.getCanonicalName(), reminder);
            startActivity(intentRemiderSettings);
        }
    };

    private class RemindersAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.tvRouteNumReminderItem){
                ((GradientDrawable)view.getBackground().getCurrent()).setColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR))));
                ((TextView)view).setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_menu:{
                finish();
                return true;
            }
            case R.id.about_menu:{
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.lvReminders){
            menu.add(Menu.NONE, CM_DELETE_ID, 0, getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case CM_DELETE_ID:{
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                deleteReminderFromDB(new String[]{String.valueOf(acmi.id)});
                databaseAccess.open();
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
            }
        }
        return true;
    }

    private void deleteReminderFromDB(final String[] params){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.deleteReminder(params);
                databaseAccess.close();
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new RemindersCursorLoader(this, databaseAccess);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scRemindersAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        scRemindersAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }
}
