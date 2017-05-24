package com.example.valdizz.busstation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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

public class StationsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView lvStations;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scStationAdapter;
    TextView tvRouteNumStations;
    TextView tvRouteNameStations;
    Bundle bundle;

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

    private void init(){
        Intent intent = getIntent();
        tvRouteNumStations.setText(intent.getStringExtra("route_num"));
        ((GradientDrawable)tvRouteNumStations.getBackground().getCurrent()).setColor(Color.parseColor("#" + intent.getStringExtra("route_color")));
        tvRouteNumStations.setTag(intent.getStringExtra("route_color"));
        tvRouteNameStations.setText(intent.getStringExtra("route_name"));
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{"name"};
        int[] to = new int[]{R.id.tvStationName};
        scStationAdapter = new SimpleCursorAdapter(this, R.layout.station_item, null, from, to, 0);
        scStationAdapter.setViewBinder(new StationsAdapterViewBinder());
        lvStations.setAdapter(scStationAdapter);
        lvStations.setOnItemClickListener(stationsListener);

        bundle = new Bundle();
        bundle.putStringArray("route_params", new String[]{tvRouteNumStations.getText().toString(), databaseAccess.DIRECTION_UP});
        getSupportLoaderManager().initLoader(0, bundle, this);
    }

    private AdapterView.OnItemClickListener stationsListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intentShedule = new Intent(StationsActivity.this, SheduleActivity.class);
            intentShedule.putExtra("route_num", tvRouteNumStations.getText());
            intentShedule.putExtra("route_name", tvRouteNameStations.getText());
            intentShedule.putExtra("route_color", tvRouteNumStations.getTag().toString());
            intentShedule.putExtra("station_name", ((TextView)view.findViewById(R.id.tvStationName)).getText());
            intentShedule.putExtra("busstation_id", String.valueOf(id));
            startActivity(intentShedule);
        }
    };

    private class StationsAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            tvRouteNameStations.setText(cursor.getString(cursor.getColumnIndex("route_name")));
            return false;
        }
    }

    public void onClickDirection(View view){
        String direction = bundle.getStringArray("route_params")[1];
        bundle.clear();
        bundle.putStringArray("route_params", new String[]{tvRouteNumStations.getText().toString(), (direction.equals(databaseAccess.DIRECTION_UP) ? databaseAccess.DIRECTION_DOWN : databaseAccess.DIRECTION_UP)});

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

    static class StationsCursorLoader extends CursorLoader {
        DatabaseAccess db;
        Bundle bundle;

        public StationsCursorLoader(Context context, DatabaseAccess db, Bundle bundle) {
            super(context);
            this.db = db;
            this.bundle = bundle;
        }

        @Override
        protected Cursor onLoadInBackground() {
            Cursor cursor = db.getStations(bundle.getStringArray("route_params"));
            return cursor;
        }
    }

}
