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
import android.util.Log;
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

import java.util.Arrays;
import java.util.Calendar;

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

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    private void initializeContentLoader() {
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME, DatabaseAccess.STATION_NAME, DatabaseAccess.REMINDER_TIME};
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
                    scRemindersAdapter.getCursor().getShort(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION)) != 0);
            Station station = new Station(
                    scRemindersAdapter.getCursor().getInt(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                    route,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                    scRemindersAdapter.getCursor().getShort(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE)) != 0,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));
            reminder = new Reminder(
                    station,
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_DATE)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_TIME)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_PERIODICITY)),
                    scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_NOTE)));

            Bundle bundle = new Bundle();
            bundle.putParcelable(Reminder.class.getCanonicalName(), reminder);
            Intent intentRemiderSettings = new Intent(RemindersActivity.this, ReminderSettingsActivity.class);
            intentRemiderSettings.putExtra(Reminder.class.getCanonicalName(), bundle);
            startActivity(intentRemiderSettings);
        }
    };

    private class RemindersAdapterViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()) {
                case R.id.tvRouteNumReminderItem:
                    ((GradientDrawable) view.getBackground().getCurrent()).setColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR))));
                    ((TextView) view).setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
                    return true;
                case R.id.tvDateTimeReminderItem:
                    String reminderTime = cursor.getString(cursor.getColumnIndex(DatabaseAccess.REMINDER_TIME));
                    String reminderDate = cursor.getString(cursor.getColumnIndex(DatabaseAccess.REMINDER_DATE));
                    String reminderPeriodicity = cursor.getString(cursor.getColumnIndex(DatabaseAccess.REMINDER_PERIODICITY));
                    if (reminderPeriodicity != null && reminderPeriodicity.length() > 0) {
                        StringBuilder reminderDateBuilder = new StringBuilder();
                        for (char ch : reminderPeriodicity.toCharArray()) {
                            switch (Character.getNumericValue(ch)) {
                                case Calendar.MONDAY:
                                    reminderDateBuilder.append(getString(R.string.reminder_monday));
                                    break;
                                case Calendar.TUESDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_tuesday));
                                    break;
                                case Calendar.WEDNESDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_wednesday));
                                    break;
                                case Calendar.THURSDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_thursday));
                                    break;
                                case Calendar.FRIDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_friday));
                                    break;
                                case Calendar.SATURDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_saturday));
                                    break;
                                case Calendar.SUNDAY:
                                    reminderDateBuilder.append(reminderDateBuilder.length() != 0 ? ", " : "").append(getString(R.string.reminder_sunday));
                                    break;
                            }
                        }
                        reminderDate = reminderPeriodicity.length() == 7 ? getString(R.string.reminder_daily) : reminderDateBuilder.toString();
                    }
                    ((TextView) view).setText(getString(R.string.reminder_datetime, reminderDate, reminderTime));
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_menu: {
                finish();
                return true;
            }
            case R.id.about_menu: {
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
        if (v.getId() == R.id.lvReminders) {
            menu.add(Menu.NONE, CM_DELETE_ID, 0, getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CM_DELETE_ID: {
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                deleteReminderFromDB(String.valueOf(acmi.id));
                removeReminders(reminder);
                getSupportLoaderManager().getLoader(0).forceLoad();
                break;
            }
        }
        return true;
    }

    private void deleteReminderFromDB(final String params) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.deleteReminder(params);
            }
        });
        thread.start();
    }

    private void removeReminders(final Reminder reminder){
        //TODO
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
