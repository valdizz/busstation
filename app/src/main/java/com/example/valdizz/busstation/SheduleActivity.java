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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SheduleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DatabaseAccess databaseAccess;
    TextView tvRouteNumShedule;
    TextView tvRouteNameShedule;
    TextView tvStationNameShedule;
    ToggleButton tbSwithcDaysShedule;
    TableLayout tlShedule;
    String stationId;
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

        Intent intent = getIntent();
        tvRouteNumShedule.setText(intent.getStringExtra("route_num"));
        ((GradientDrawable)tvRouteNumShedule.getBackground().getCurrent()).setColor(Color.parseColor("#" + intent.getStringExtra("route_color")));
        tvRouteNameShedule.setText(intent.getStringExtra("route_name"));
        tvStationNameShedule.setText(intent.getStringExtra("station_name"));
        stationId = intent.getStringExtra("station_id");

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        timeToDepartureDialog = new TimeToDepartureDialog();
        bundle = new Bundle();
        day = isWeekend() ? databaseAccess.SHEDULE_WEEKDAY : databaseAccess.SHEDULE_WORKDAY;
        tbSwithcDaysShedule.setChecked(isWeekend());
        createShedule(hour = 4, day);

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

    private void createShedule(int hour, String day) {
        bundle.clear();
        bundle.putStringArray("shedule_params", new String[]{stationId, (hour < 10 ? "0" + String.valueOf(hour) + "%" : String.valueOf(hour) + "%"), day});
        getSupportLoaderManager().restartLoader(0, bundle, this).forceLoad();
    }

    private void createSheduleLine(Cursor data) {
        String time;
        if ((data!=null) && (data.getCount()>0)) {
            //создаем строку
            TableRow tableRow = new TableRow(this);
            tableRow.setTag(hour);
            TableRow.LayoutParams trParams = new TableRow.LayoutParams();
            trParams.gravity = Gravity.CENTER;
            tableRow.setLayoutParams(trParams);

            //добавим заголовок строки для каждого часа
            tableRow.addView(tvSheduleHour(getHourFormatHH(hour)));

            //добавляем ячейки со временем
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
                        Log.d("DDD", "Before hour: "+timeHourDiff +" min:"+ timeMinDiff);
                        timeMinDiff += 60;
                        timeHourDiff -= 1;
                        Log.d("DDD", "After hour: "+timeHourDiff +" min:"+ timeMinDiff);
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
        Log.d("DDD", "Iteration stop "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
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
        if (((ToggleButton)view).isChecked()){
            day = databaseAccess.SHEDULE_WEEKDAY;
        } else {
            day = databaseAccess.SHEDULE_WORKDAY;
        }
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
