package com.example.valdizz.busstation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Dialogs.TimeToDepartureDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SheduleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DatabaseAccess databaseAccess;
    Menu menu;
    TextView tvRouteNumShedule;
    TextView tvRouteNameShedule;
    TextView tvStationNameShedule;
    ToggleButton tbSwithcDaysShedule;
    TableLayout tlShedule;
    String busstation_id;
    boolean is_favorite_station;
    Bundle bundle;
    int hour;
    String day;
    Handler updateHandler;
    TimeToDepartureDialog timeToDepartureDialog;
    public static final long UPDATE_PERIOD = 60*1000L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shedule);

        tvRouteNumShedule = (TextView)findViewById(R.id.tvRouteNumShedule);
        tvRouteNameShedule = (TextView)findViewById(R.id.tvRouteNameShedule);
        tvStationNameShedule = (TextView)findViewById(R.id.tvStationNameShedule);
        tbSwithcDaysShedule = (ToggleButton)findViewById(R.id.switchDaysShedule);
        tlShedule = (TableLayout) findViewById(R.id.tlShedule);

        init();
        createShedule(hour = 4, day);
        initializeHandler();
    }

    private void init(){
        Intent intent = getIntent();
        tvRouteNumShedule.setText(intent.getStringExtra("route_num"));
        ((GradientDrawable)tvRouteNumShedule.getBackground().getCurrent()).setColor(Color.parseColor("#" + intent.getStringExtra("route_color")));
        tvRouteNameShedule.setText(intent.getStringExtra("route_name"));
        tvStationNameShedule.setText(intent.getStringExtra("station_name"));
        busstation_id = intent.getStringExtra("busstation_id");

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        is_favorite_station = databaseAccess.isFavoriteStation(new String[] {busstation_id});
        timeToDepartureDialog = new TimeToDepartureDialog();
        bundle = new Bundle();
        day = isWeekend() ? databaseAccess.SHEDULE_WEEKDAY : databaseAccess.SHEDULE_WORKDAY;
        tbSwithcDaysShedule.setChecked(isWeekend());
    }

    private void initializeHandler(){
        updateHandler = new Handler();
        long next = SystemClock.uptimeMillis() + (SheduleActivity.UPDATE_PERIOD - System.currentTimeMillis() % SheduleActivity.UPDATE_PERIOD) + 1000;
        updateHandler.postAtTime(updateRunnable, next);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            String currentTime = getCurrentTime();
            int currentHour = Integer.parseInt(currentTime.substring(0,2));

            List<TableRow> tableRows = new ArrayList<>();
            for (int i=currentHour-1; i<=currentHour; i++){
                TableRow tr = (TableRow)tlShedule.findViewWithTag(i);
                if (tr!=null)
                    tableRows.add(tr);
            }

            for (TableRow tableRow : tableRows){
                for (int j = 1; j < tableRow.getChildCount(); j++) {
                    TextView tv = (TextView) tableRow.getChildAt(j);
                    tv.setBackgroundResource(isLater(tv.getText().toString(), currentTime) ? R.drawable.shedule_item : R.drawable.shedule_item_past);
                }
            }
            updateHandler.postDelayed(updateRunnable, SheduleActivity.UPDATE_PERIOD);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.shedule_actionbar_menu, menu);
        menu.findItem(R.id.add_menu).setIcon(is_favorite_station ? R.drawable.remove_icon : R.drawable.add_icon);
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
            case R.id.add_menu:{
                databaseAccess.open();
                databaseAccess.setFavoriteStation(is_favorite_station ? DatabaseAccess.FAVOURITE_OFF : DatabaseAccess.FAVOURITE_ON, busstation_id);
                menu.findItem(R.id.add_menu).setIcon(is_favorite_station ? R.drawable.add_icon : R.drawable.remove_icon);
                Toast.makeText(this, getString(is_favorite_station ? R.string.toast_removefromfavorites : R.string.toast_addtofavorites, tvStationNameShedule.getText()), Toast.LENGTH_SHORT).show();
                is_favorite_station = !is_favorite_station;
                databaseAccess.close();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createShedule(int hour, String day) {
        bundle.clear();
        bundle.putStringArray("shedule_params", new String[]{busstation_id, (hour < 10 ? "0" + String.valueOf(hour) + "%" : String.valueOf(hour) + "%"), day});
        getSupportLoaderManager().restartLoader(0, bundle, this).forceLoad();
    }

    private void createSheduleLine(Cursor data) {
        String time;
        if ((data!=null) && (data.getCount()>0)) {
            //add line
            TableRow tableRow = new TableRow(this);
            tableRow.setTag(hour);
            TableRow.LayoutParams trParams = new TableRow.LayoutParams();
            trParams.gravity = Gravity.CENTER;
            tableRow.setLayoutParams(trParams);

            //add line header (every hour)
            tableRow.addView(tvSheduleHour(getHourFormatHH(hour)));

            //add cells with time
            data.moveToFirst();
            while (!data.isAfterLast()) {
                time = data.getString(data.getColumnIndex("time"));
                tableRow.addView(tvSheduleTime(time, (isLater(time, getCurrentTime()) ? R.drawable.shedule_item : R.drawable.shedule_item_past)));
                data.moveToNext();
            }
            tlShedule.addView(tableRow);
        }
    }

    private TextView tvSheduleHour(String hour){
        TextView textViewHour = new TextView(this);
        textViewHour.setText(hour);
        textViewHour.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        textViewHour.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParamsHour = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParamsHour.setMargins(5,5,5,5);
        textViewHour.setLayoutParams(tlParamsHour);
        textViewHour.setBackgroundResource(R.drawable.shedule_item_hour);
        return textViewHour;
    }

    private TextView tvSheduleTime(final String time, int idBgndRes){
        TextView textViewTime = new TextView(this);
        textViewTime.setText(time);
        textViewTime.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        textViewTime.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParams.setMargins(5,5,5,5);
        textViewTime.setLayoutParams(tlParams);
        textViewTime.setBackgroundResource(idBgndRes);
        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTime = getCurrentTime();
                if (isLater(time, currentTime)){
                    int timeH = Integer.valueOf(time.substring(0,2));
                    int curTimeH = Integer.valueOf(currentTime.substring(0,2));
                    int timeHourDiff = (timeH<=3 ? timeH+24 : timeH) - (curTimeH<=3 ? curTimeH+24 : curTimeH);
                    int timeMinDiff = Integer.valueOf(time.substring(3)) - Integer.valueOf(currentTime.substring(3));
                    if (timeMinDiff < 0) {
                        timeMinDiff += 60;
                        timeHourDiff -= 1;
                    }
                    timeToDepartureDialog.setMessage(getString(R.string.dialog_message, timeHourDiff, timeMinDiff));
                }
                else {
                    timeToDepartureDialog.setMessage(getString(R.string.dialog_message_left));
                }

                timeToDepartureDialog.show(getSupportFragmentManager(), "timeToDepartureDialog");
            }
        });
        return textViewTime;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new SheduleCursorLoader(this, databaseAccess, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        createSheduleLine(data);
        if (hour == 23)
            hour = -1;
        if (hour != 3)
            createShedule(++hour, day);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
        updateHandler.removeCallbacks(updateRunnable);
    }

    static class SheduleCursorLoader extends CursorLoader {
        DatabaseAccess db;
        Bundle bundle;

        public SheduleCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
            super(context);
            this.db = db;
            this.bundle = bundle;
        }

        @Override
        protected Cursor onLoadInBackground() {
            Cursor cursor = db.getShedule(bundle.getStringArray("shedule_params"));
            return cursor;
        }
    }

    public void onClickSwitchDays(View view){
        if (((ToggleButton)view).isChecked())
            day = databaseAccess.SHEDULE_WEEKDAY;
        else
            day = databaseAccess.SHEDULE_WORKDAY;
        tlShedule.removeAllViews();
        databaseAccess.open();
        createShedule(hour = 4, day);
    }

    private boolean isWeekend(){
        String dayOfWeek = new SimpleDateFormat("F").format(new Date());
        return (dayOfWeek.equals("6") || dayOfWeek.equals("7"));
    }

    private String getCurrentTime(){
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        return currentTime;
    }

    private String getHourFormatHH(int hour){
        return hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
    }

    public boolean isLater(String time, String currentTime){
        int timeH = Integer.valueOf(time.substring(0,2));
        int curTimeH = Integer.valueOf(currentTime.substring(0,2));
        int timeM = Integer.valueOf(time.substring(3));
        int curTimeM = Integer.valueOf(currentTime.substring(3));
        if (timeH <= 3)
            timeH = timeH + 24;
        if (curTimeH <= 3)
            curTimeH = curTimeH + 24;

        if (timeH != curTimeH)
            return (timeH > curTimeH);
        else
            return (timeM > curTimeM);
    }
}
