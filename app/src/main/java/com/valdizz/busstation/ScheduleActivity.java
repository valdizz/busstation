package com.valdizz.busstation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.valdizz.busstation.Database.DatabaseAccess;
import com.valdizz.busstation.Dialogs.TimeToDepartureDialog;
import com.valdizz.busstation.Dialogs.TransferRoutesDialog;
import com.valdizz.busstation.Model.Schedule;
import com.valdizz.busstation.Model.Station;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScheduleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final long UPDATE_PERIOD = 60*1000L;

    DatabaseAccess databaseAccess;
    Menu menu;
    TextView tvRouteNumSchedule, tvRouteNameSchedule, tvStationNameSchedule, tvNoSchedule;
    TableLayout tlSchedule;
    LinearLayout llTransferRoutes, llCaptionSchedule;
    Bundle bundle;
    Handler updateHandler;
    TimeToDepartureDialog timeToDepartureDialog;
    Station station;
    Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        tvRouteNumSchedule = (TextView)findViewById(R.id.tvRouteNumSchedule);
        tvRouteNameSchedule = (TextView)findViewById(R.id.tvRouteNameSchedule);
        tvStationNameSchedule = (TextView)findViewById(R.id.tvStationNameSchedule);
        tvNoSchedule = (TextView)findViewById(R.id.tvNoSchedule);
        tlSchedule = (TableLayout) findViewById(R.id.tlSchedule);
        llCaptionSchedule = (LinearLayout) findViewById(R.id.llCaptionSchedule);
        llTransferRoutes = (LinearLayout) findViewById(R.id.llTransferRoutes);
        llTransferRoutes.setOnClickListener(onTransferRoutesClickListener);

        init();
        createTransferRoutes();
        createSchedule(String.valueOf(schedule.getStation().getId()), schedule.isWeekday() ? "1" : "0");
        initializeHandler();
    }

    private void init(){
        station = getIntent().getBundleExtra(Station.class.getCanonicalName()).getParcelable(Station.class.getCanonicalName());
        schedule = new Schedule(station, "", isWeekend(), "");
        bundle = new Bundle();

        tvRouteNumSchedule.setText(station.getRoute().getNumber());
        ((GradientDrawable) tvRouteNumSchedule.getBackground().getCurrent()).setColor(Color.parseColor("#" + station.getRoute().getColor()));
        tvRouteNameSchedule.setText(station.getRoute().getName());
        tvStationNameSchedule.setText(station.getName());
        tvNoSchedule.setVisibility(View.GONE);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        timeToDepartureDialog = new TimeToDepartureDialog();
    }

    private void initializeHandler(){
        updateHandler = new Handler();
        long next = SystemClock.uptimeMillis() + (ScheduleActivity.UPDATE_PERIOD - System.currentTimeMillis() % ScheduleActivity.UPDATE_PERIOD) + 1000;
        updateHandler.postAtTime(updateRunnable, next);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            String currentTime = getCurrentTime();
            int currentHour = Integer.parseInt(currentTime.substring(0,2));

            List<TableRow> tableRows = new ArrayList<>();
            for (int i=currentHour-1; i<=currentHour; i++){
                TableRow tr = (TableRow) tlSchedule.findViewWithTag(i);
                if (tr!=null)
                    tableRows.add(tr);
            }

            for (TableRow tableRow : tableRows){
                for (int j = 1; j < tableRow.getChildCount(); j++) {
                    TextView tv = (TextView) tableRow.getChildAt(j);
                    tv.setBackgroundResource(isLater(tv.getText().toString(), currentTime) ? R.drawable.schedule_item : R.drawable.schedule_item_past);
                }
            }
            updateHandler.postDelayed(updateRunnable, ScheduleActivity.UPDATE_PERIOD);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
        menu.findItem(R.id.add_menu).setIcon(schedule.getStation().isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        menu.findItem(R.id.day_menu).setIcon(schedule.isWeekday() ? R.drawable.ic_menu_weekday : R.drawable.ic_menu_workday);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
            case R.id.about_menu:{
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.add_menu:{
                new FavoriteStationTask().execute(schedule.getStation().isFavorite() ? "0" : "1", String.valueOf(schedule.getStation().getId()));
                return true;
            }
            case R.id.day_menu:{
                tvNoSchedule.setVisibility(View.GONE);
                schedule.setWeekday(!schedule.isWeekday());
                menu.findItem(R.id.day_menu).setIcon(schedule.isWeekday() ? R.drawable.ic_menu_weekday : R.drawable.ic_menu_workday);
                tlSchedule.removeAllViews();
                databaseAccess.open();
                createSchedule(String.valueOf(schedule.getStation().getId()), schedule.isWeekday() ? "1" : "0");
                Toast.makeText(ScheduleActivity.this, getString(schedule.isWeekday() ? R.string.toast_weekend : R.string.toast_workingdays), Toast.LENGTH_SHORT).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createTransferRoutes(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Cursor transferRoutes = databaseAccess.getTransferRoutesNumbers(new String[]{String.valueOf(station.getName()), station.getRoute().getNumber()});

                if (transferRoutes.getCount() == 0){
                    llCaptionSchedule.removeView(llTransferRoutes);
                    return;
                }

                llTransferRoutes.addView(tvTransferRoutes(getString(R.string.route_transfer),""));
                while (!transferRoutes.isAfterLast()) {
                    llTransferRoutes.addView(tvTransferRoutes(transferRoutes.getString(transferRoutes.getColumnIndex("route_number")),transferRoutes.getString(transferRoutes.getColumnIndex("route_color"))));
                    transferRoutes.moveToNext();
                }
            }
        });
    }

    private TextView tvTransferRoutes(String text, String color){
        TextView transferRoutes = new TextView(this);
        transferRoutes.setText(text);
        transferRoutes.setTextColor(Color.BLACK);
        transferRoutes.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParamsHour = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParamsHour.setMargins(2,2,2,2);
        transferRoutes.setPadding(2,2,2,2);
        transferRoutes.setMinWidth(pxFromDp(24));
        transferRoutes.setLayoutParams(tlParamsHour);
        if (color.length()>0)
            transferRoutes.setBackgroundColor(Color.parseColor("#" + color));
        return transferRoutes;
    }

    View.OnClickListener onTransferRoutesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle transferRoutesListDialogArgs = new Bundle();
            transferRoutesListDialogArgs.putString(DatabaseAccess.ROUTE_NUMBER, station.getRoute().getNumber());
            transferRoutesListDialogArgs.putString(DatabaseAccess.STATION_NAME, station.getName());
            TransferRoutesDialog transferRoutesDialog = new TransferRoutesDialog();
            transferRoutesDialog.setArguments(transferRoutesListDialogArgs);
            transferRoutesDialog.show(getSupportFragmentManager(), station.getName());
        }
    };

    private void createSchedule(String station_id, String day) {
        bundle.clear();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{station_id, day});
        getSupportLoaderManager().restartLoader(0, bundle, this).forceLoad();
    }

    private void createScheduleTable(Map<String, List<String>> scheduleMap) {
        int hour;
        String time, description;
        for (int i=0; i<24; i++){
            hour = i+4<24 ? i+4 : i-20; //start at 4 am
            if (scheduleMap.get(getHourFormatHH(hour)) != null){
                //add row
                TableRow tableRow = new TableRow(this);
                tableRow.setTag(getHourFormatHH(hour));
                TableRow.LayoutParams trParams = new TableRow.LayoutParams();
                trParams.gravity = Gravity.CENTER;
                tableRow.setLayoutParams(trParams);
                //add row header (hour)
                tableRow.addView(tvScheduleHour(getHourFormatHH(hour)));
                //add cells with time
                for (String time_description : scheduleMap.get(getHourFormatHH(hour))){
                    if (time_description.length()==5) {
                        time = time_description;
                        description = "";
                    }
                    else {
                        time = time_description.substring(0,5);
                        description = time_description.substring(5);
                    }
                    tableRow.addView(tvScheduleTime(time, description, (isLater(time, getCurrentTime()) ? R.drawable.schedule_item : R.drawable.schedule_item_past)));
                }
                tlSchedule.addView(tableRow);
            }
        }
    }

    private TextView tvScheduleHour(String hour){
        TextView textViewHour = new TextView(this);
        textViewHour.setText(hour);
        textViewHour.setTextColor(Color.BLACK);
        textViewHour.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParamsHour = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParamsHour.setMargins(5,5,5,5);
        textViewHour.setLayoutParams(tlParamsHour);
        textViewHour.setBackgroundResource(R.drawable.schedule_item_hour);
        return textViewHour;
    }

    private TextView tvScheduleTime(final String time, final String description, int idBgndRes){
        TextView textViewTime = new TextView(this);
        textViewTime.setText(time);
        if (description!=null && description.length()>0){
            textViewTime.setPaintFlags(textViewTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        textViewTime.setTextColor(Color.BLACK);
        textViewTime.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParams.setMargins(5,5,5,5);
        textViewTime.setLayoutParams(tlParams);
        textViewTime.setBackgroundResource(idBgndRes);
        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedule.setTime(time);
                schedule.setDescription(description);
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
                    timeToDepartureDialog.setTitle(getString(R.string.dialog_message, timeHourDiff, timeMinDiff));
                }
                else {
                    timeToDepartureDialog.setTitle(getString(R.string.dialog_message_left));
                }
                timeToDepartureDialog.setSchedule(schedule);
                timeToDepartureDialog.show(getSupportFragmentManager(), "timeToDepartureDialog");
            }
        });
        return textViewTime;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new ScheduleCursorLoader(this, databaseAccess, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Map<String, List<String>> scheduleMap = new HashMap<>();
        String hour;
        StringBuilder time;
        List<String> hourSchedule;
        while (!data.isAfterLast()) {
            hour = data.getString(data.getColumnIndex(DatabaseAccess.SCHEDULE_TIME)).substring(0,2);
            time = new StringBuilder(data.getString(data.getColumnIndex(DatabaseAccess.SCHEDULE_TIME)));
            if (data.getString(data.getColumnIndex(DatabaseAccess.SCHEDULE_DESCRIPTION)).length()>0)
                time.append(data.getString(data.getColumnIndex(DatabaseAccess.SCHEDULE_DESCRIPTION)));
            if (scheduleMap.get(hour)==null)
                hourSchedule = new ArrayList<>();
            else
                hourSchedule = scheduleMap.get(hour);
            hourSchedule.add(time.toString());
            scheduleMap.put(hour, hourSchedule);
            data.moveToNext();
        }

        if (scheduleMap.isEmpty())
            tvNoSchedule.setVisibility(View.VISIBLE);
        else
            createScheduleTable(scheduleMap);
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

    static class ScheduleCursorLoader extends CursorLoader {
        DatabaseAccess db;
        Bundle bundle;

        ScheduleCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
            super(context);
            this.db = db;
            this.bundle = bundle;
        }

        @Override
        protected Cursor onLoadInBackground() {
            return db.getSchedule(bundle.getStringArray(DatabaseAccess.BUNDLE_PARAMS));
        }
    }

    private int pxFromDp(float dp) {
        return (int) (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private boolean isWeekend(){
        Calendar dayOfWeek = Calendar.getInstance();
        if (dayOfWeek.get(Calendar.HOUR_OF_DAY) > 3 && dayOfWeek.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            return true;
        else if (dayOfWeek.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            return true;
        else if (dayOfWeek.get(Calendar.HOUR_OF_DAY) <= 3 && dayOfWeek.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
            return true;
        else if (isHoliday(dayOfWeek, 1, Calendar.JANUARY))
            return true;
        else if (isHoliday(dayOfWeek, 7, Calendar.JANUARY))
            return true;
        else if (isHoliday(dayOfWeek, 8, Calendar.MARCH))
            return true;
        else if (isHoliday(dayOfWeek, 1, Calendar.MAY))
            return true;
        else if (isHoliday(dayOfWeek, 9, Calendar.MAY))
            return true;
        else if (isHoliday(dayOfWeek, 3, Calendar.JULY))
            return true;
        else if (isHoliday(dayOfWeek, 7, Calendar.NOVEMBER))
            return true;
        else if (isHoliday(dayOfWeek, 25, Calendar.DECEMBER))
            return true;
        else
            return false;
    }

    private boolean isHoliday(Calendar day, int dayOfMonth, int month) {
        if (day.get(Calendar.HOUR_OF_DAY) > 3 && day.get(Calendar.DAY_OF_MONTH) == dayOfMonth && day.get(Calendar.MONTH) == month)
            return true;
        else if (day.get(Calendar.HOUR_OF_DAY) <= 3 && day.get(Calendar.DAY_OF_MONTH) == dayOfMonth+1)
            return true;
        else
            return false;
    }

    private String getCurrentTime(){
        return new SimpleDateFormat("HH:mm").format(new Date());
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
            return (timeM >= curTimeM);
    }

    class FavoriteStationTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            schedule.getStation().setFavorite(!schedule.getStation().isFavorite());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            menu.findItem(R.id.add_menu).setIcon(schedule.getStation().isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            Toast.makeText(ScheduleActivity.this, getString(!schedule.getStation().isFavorite() ? R.string.toast_removefromfavorites : R.string.toast_addtofavorites, schedule.getStation().getName()), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            databaseAccess.open();
            databaseAccess.setFavoriteStation(strings[0], strings[1]);
            return null;
        }
    }

}
