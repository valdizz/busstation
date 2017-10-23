package com.valdizz.busstation;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.valdizz.busstation.Database.DatabaseAccess;
import com.valdizz.busstation.Database.RemindersCursorLoader;
import com.valdizz.busstation.Dialogs.RemoveReminderDialog;
import com.valdizz.busstation.Model.Reminder;
import com.valdizz.busstation.Model.Route;
import com.valdizz.busstation.Model.Station;

import java.util.Calendar;

public class RemindersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    SwipeMenuListView lvReminders;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRemindersAdapter;
    Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        lvReminders = (SwipeMenuListView) findViewById(R.id.lvReminders);
        lvReminders.setMenuCreator(creator);
        lvReminders.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        lvReminders.setCloseInterpolator(new BounceInterpolator());
        lvReminders.setEmptyView(findViewById(android.R.id.empty));
        lvReminders.setOnMenuItemClickListener(onMenuItemClickListener);
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
        lvReminders.setOnItemLongClickListener(reminderOnItemLongClickListener);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
            deleteItem.setWidth(pxFromDp(60));
            deleteItem.setIcon(android.R.drawable.ic_delete);
            menu.addMenuItem(deleteItem);
        }
    };

    private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    new RemoveReminderDialog().show(getSupportFragmentManager(), String.valueOf(lvReminders.getItemIdAtPosition(position)));
                    break;
            }
            return false;
        }
    };

    private Reminder getReminderFromAdapter(SimpleCursorAdapter scRemindersAdapter){
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
        Reminder reminder = new Reminder(
                station,
                scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_DATE)),
                scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_TIME)),
                scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_PERIODICITY)),
                scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_NOTE)));
        return reminder;
    }

    private AdapterView.OnItemClickListener reminderOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            reminder = getReminderFromAdapter(scRemindersAdapter);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Reminder.class.getCanonicalName(), reminder);
            Intent intentRemiderSettings = new Intent(RemindersActivity.this, ReminderSettingsActivity.class);
            intentRemiderSettings.putExtra(Reminder.class.getCanonicalName(), bundle);
            startActivity(intentRemiderSettings);
        }
    };

    private AdapterView.OnItemLongClickListener reminderOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            new RemoveReminderDialog().show(getSupportFragmentManager(), String.valueOf(String.valueOf(id)));
            return true;
        }
    };

    public void removeReminder(String id) {
        reminder = getReminderFromAdapter(scRemindersAdapter);
        reminder.remove(this);
        reminder.removeFromDB(databaseAccess, id);
        getSupportLoaderManager().restartLoader(0, null, this).forceLoad();
    }

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
    public  boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
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

    private int pxFromDp(float dp) {
        return (int) (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }
}
