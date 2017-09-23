package com.example.valdizz.busstation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
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

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Dialogs.TimeToDepartureDialog;
import com.example.valdizz.busstation.Dialogs.TransferRoutesDialog;
import com.example.valdizz.busstation.Model.Shedule;
import com.example.valdizz.busstation.Model.Station;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SheduleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final long UPDATE_PERIOD = 60*1000L;

    DatabaseAccess databaseAccess;
    Menu menu;
    TextView tvRouteNumShedule;
    TextView tvRouteNameShedule;
    TextView tvStationNameShedule;
    TableLayout tlShedule;
    LinearLayout llTransferRoutes, llCaptionShedule;
    Bundle bundle;
    int hour;
    Handler updateHandler;
    TimeToDepartureDialog timeToDepartureDialog;
    Station station;
    Shedule shedule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shedule);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        tvRouteNumShedule = (TextView)findViewById(R.id.tvRouteNumShedule);
        tvRouteNameShedule = (TextView)findViewById(R.id.tvRouteNameShedule);
        tvStationNameShedule = (TextView)findViewById(R.id.tvStationNameShedule);
        tlShedule = (TableLayout) findViewById(R.id.tlShedule);
        llCaptionShedule = (LinearLayout) findViewById(R.id.llCaptionShedule);
        llTransferRoutes = (LinearLayout) findViewById(R.id.llTransferRoutes);
        llTransferRoutes.setOnClickListener(onTransferRoutesClickListener);

        init();
        createTransferRoutes();
        createShedule(hour = 4, shedule.isWeekday() ? "1" : "0");
        initializeHandler();
    }

    private void init(){
        station = getIntent().getBundleExtra(Station.class.getCanonicalName()).getParcelable(Station.class.getCanonicalName());
        shedule = new Shedule(station, "", isWeekend(), "");
        bundle = new Bundle();

        tvRouteNumShedule.setText(station.getRoute().getNumber());
        ((GradientDrawable)tvRouteNumShedule.getBackground().getCurrent()).setColor(Color.parseColor("#" + station.getRoute().getColor()));
        tvRouteNameShedule.setText(station.getRoute().getName());
        tvStationNameShedule.setText(station.getName());

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        timeToDepartureDialog = new TimeToDepartureDialog();
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
        getMenuInflater().inflate(R.menu.shedule_menu, menu);
        menu.findItem(R.id.add_menu).setIcon(shedule.getStation().isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        menu.findItem(R.id.day_menu).setIcon(shedule.isWeekday() ? R.drawable.ic_menu_weekday : R.drawable.ic_menu_workday);
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
                shedule.getStation().setFavorite(!shedule.getStation().isFavorite());
                setFavoriteStation();
                return true;
            }
            case R.id.day_menu:{
                shedule.setWeekday(!shedule.isWeekday());
                menu.findItem(R.id.day_menu).setIcon(shedule.isWeekday() ? R.drawable.ic_menu_weekday : R.drawable.ic_menu_workday);
                tlShedule.removeAllViews();
                databaseAccess.open();
                createShedule(hour = 4, shedule.isWeekday() ? "1" : "0");
                Toast.makeText(SheduleActivity.this, getString(shedule.isWeekday() ? R.string.toast_weekend : R.string.toast_workingdays), Toast.LENGTH_SHORT).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFavoriteStation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //databaseAccess.open();
                databaseAccess.setFavoriteStation(shedule.getStation().isFavorite() ? "1" : "0", String.valueOf(shedule.getStation().getId()));
                menu.findItem(R.id.add_menu).setIcon(shedule.getStation().isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                Toast.makeText(SheduleActivity.this, getString(!shedule.getStation().isFavorite() ? R.string.toast_removefromfavorites : R.string.toast_addtofavorites, shedule.getStation().getName()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTransferRoutes(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Cursor transferRoutes = databaseAccess.getTransferRoutesNumbers(new String[]{String.valueOf(station.getName()), station.getRoute().getNumber()});

                if (transferRoutes.getCount() == 0){
                    llCaptionShedule.removeView(llTransferRoutes);
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

    private int pxFromDp(float dp) {
        return (int) (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private void createShedule(int hour, String day) {
        bundle.clear();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{String.valueOf(shedule.getStation().getId()), (hour < 10 ? "0" + String.valueOf(hour) + "%" : String.valueOf(hour) + "%"), day});
        getSupportLoaderManager().restartLoader(0, bundle, this).forceLoad();
    }

    private void createSheduleLine(Cursor data) {
        String time, description;
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
                time = data.getString(data.getColumnIndex(DatabaseAccess.SHEDULE_TIME));
                description = data.getString(data.getColumnIndex(DatabaseAccess.SHEDULE_DESCRIPTION));
                tableRow.addView(tvSheduleTime(time, description, (isLater(time, getCurrentTime()) ? R.drawable.shedule_item : R.drawable.shedule_item_past)));
                data.moveToNext();
            }
            tlShedule.addView(tableRow);
        }
    }

    private TextView tvSheduleHour(String hour){
        TextView textViewHour = new TextView(this);
        textViewHour.setText(hour);
        textViewHour.setTextColor(Color.BLACK);
        textViewHour.setGravity(Gravity.CENTER);
        TableRow.LayoutParams tlParamsHour = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlParamsHour.setMargins(5,5,5,5);
        textViewHour.setLayoutParams(tlParamsHour);
        textViewHour.setBackgroundResource(R.drawable.shedule_item_hour);
        return textViewHour;
    }

    private TextView tvSheduleTime(final String time, final String description, int idBgndRes){
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
                shedule.setTime(time);
                shedule.setDescription(description);
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
                timeToDepartureDialog.setShedule(shedule);
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
            createShedule(++hour, shedule.isWeekday() ? "1" : "0");
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
            Cursor cursor = db.getShedule(bundle.getStringArray(DatabaseAccess.BUNDLE_PARAMS));
            return cursor;
        }
    }

    private boolean isWeekend(){
        Calendar dayOfWeek = Calendar.getInstance();
        return dayOfWeek.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY || dayOfWeek.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY;
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
            return (timeM > curTimeM);
    }
}
