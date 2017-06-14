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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.StationsCursorLoader;
import com.example.valdizz.busstation.Model.Route;
import com.example.valdizz.busstation.Model.Station;

public class StationsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView lvStations;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scStationAdapter;
    TextView tvRouteNumStations;
    TextView tvRouteNameStations;
    Bundle bundle;
    Route route;
    Station station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        lvStations = (ListView) findViewById(R.id.lvStations);
        tvRouteNumStations = (TextView)findViewById(R.id.tvRouteNumStations);
        tvRouteNameStations = (TextView)findViewById(R.id.tvRouteNameStations);

        init();
        initializeContentLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().getLoader(0).forceLoad();
    }


    private void init(){
        route = getIntent().getBundleExtra(Route.class.getCanonicalName()).getParcelable(Route.class.getCanonicalName());

        tvRouteNumStations.setText(route.getNumber());
        ((GradientDrawable)tvRouteNumStations.getBackground().getCurrent()).setColor(Color.parseColor("#" + route.getColor()));
        tvRouteNameStations.setText(route.getName());
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
            Intent intentShedule = new Intent(StationsActivity.this, SheduleActivity.class);
            intentShedule.putExtra(Station.class.getCanonicalName(), bundle);
            startActivity(intentShedule);
        }
    };

    private class StationsAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            tvRouteNameStations.setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NAME)));
            return false;
        }
    }

    public void onClickDirection(View view){
        route.setDirection(!route.isDirection());
        bundle.clear();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{route.getNumber(), route.isDirection() ? "1" : "0"});
        databaseAccess.open();
        getSupportLoaderManager().restartLoader(0, bundle, this).forceLoad();
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
