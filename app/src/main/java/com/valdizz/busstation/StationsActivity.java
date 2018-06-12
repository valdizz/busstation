package com.valdizz.busstation;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.valdizz.busstation.Database.DatabaseAccess;
import com.valdizz.busstation.Database.StationsCursorLoader;
import com.valdizz.busstation.Model.Route;
import com.valdizz.busstation.Model.Station;

public class StationsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final OvershootInterpolator interpolator = new OvershootInterpolator();
    ListView lvStations;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scStationAdapter;
    TextView tvRouteNumStations;
    TextView tvRouteNameStations;
    Bundle bundle;
    Route route;
    Station station;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        lvStations = (ListView) findViewById(R.id.lvStations);
        tvRouteNumStations = (TextView)findViewById(R.id.tvRouteNumStations);
        tvRouteNameStations = (TextView)findViewById(R.id.tvRouteNameStations);
        fab = (FloatingActionButton)findViewById(R.id.fab);

        init();
        initializeContentLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseAccess.open();
        getSupportLoaderManager().getLoader(0).forceLoad();
    }


    private void init(){
        route = getIntent().getBundleExtra(Route.class.getCanonicalName()).getParcelable(Route.class.getCanonicalName());
        tvRouteNumStations.setText(route.getNumber());
        ((GradientDrawable)tvRouteNumStations.getBackground().getCurrent()).setColor(Color.parseColor("#" + route.getColor()));
        tvRouteNameStations.setText(route.getName());
        fab.setOnClickListener(fabListener);
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{DatabaseAccess.STATION_NAME};
        int[] to = new int[]{R.id.tvStationName};
        scStationAdapter = new SimpleCursorAdapter(this, R.layout.station_item, null, from, to, 0);
        scStationAdapter.setViewBinder(new StationsAdapterViewBinder());
        lvStations.setAdapter(scStationAdapter);
        lvStations.setOnItemClickListener(stationsListener);

        bundle = new Bundle();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{route.getNumber(), route.isDirection() ? "1" : "0"});
        getSupportLoaderManager().initLoader(0, bundle, this);
    }

    private AdapterView.OnItemClickListener stationsListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            station = new Station(
                    scStationAdapter.getCursor().getInt(scStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                    route,
                    scStationAdapter.getCursor().getString(scStationAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                    scStationAdapter.getCursor().getShort(scStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE))!=0,
                    scStationAdapter.getCursor().getString(scStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));

            Bundle bundle = new Bundle();
            bundle.putParcelable(Station.class.getCanonicalName(), station);
            Intent intentShedule = new Intent(StationsActivity.this, ScheduleActivity.class);
            intentShedule.putExtra(Station.class.getCanonicalName(), bundle);
            startActivity(intentShedule);
        }
    };

    private class StationsAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            tvRouteNameStations.setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NAME)));
            route.setName(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NAME)));
            return false;
        }
    }

    private View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            route.setDirection(!route.isDirection());
            bundle.clear();
            bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{route.getNumber(), route.isDirection() ? "1" : "0"});
            databaseAccess.open();
            getSupportLoaderManager().restartLoader(0, bundle, StationsActivity.this).forceLoad();

            ViewCompat.animate(fab).
                    rotationBy(180f).
                    withLayer().
                    setDuration(300).
                    setInterpolator(interpolator).
                    start();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new StationsCursorLoader(this, databaseAccess, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scStationAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        scStationAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

}
