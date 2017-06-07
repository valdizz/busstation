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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.RemindersCursorLoader;

public class RemindersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView lvReminders;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRemindersAdapter;

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
        lvReminders.setOnItemClickListener(remindersListener);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private AdapterView.OnItemClickListener remindersListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intentRemider = new Intent(RemindersActivity.this, ReminderSettingsActivity.class);
            intentRemider.putExtra("route_num", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
            intentRemider.putExtra("route_name", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)));
            intentRemider.putExtra("route_color", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)));
            intentRemider.putExtra("station_name", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)));
            intentRemider.putExtra("busstation_id", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_BUSSTATIONS_ID)));
            intentRemider.putExtra("reminder_date", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_DATE)));
            intentRemider.putExtra("reminder_time", scRemindersAdapter.getCursor().getString(scRemindersAdapter.getCursor().getColumnIndex(DatabaseAccess.REMINDER_TIME)));
            startActivity(intentRemider);
        }
    };

    private class RemindersAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String color = cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR));
            String number = cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER));
            if (view.getId() == R.id.tvRouteNumReminderItem){
                ((GradientDrawable)view.getBackground().getCurrent()).setColor(Color.parseColor("#" + color));
                ((TextView)view).setText(number);
                view.setTag(color);
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
