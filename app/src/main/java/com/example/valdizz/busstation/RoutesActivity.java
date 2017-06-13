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
import com.example.valdizz.busstation.Database.RoutesCursorLoader;
import com.example.valdizz.busstation.Model.Route;


public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView lvRoutes;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRoutesAdapter;
    Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        lvRoutes = (ListView) findViewById(R.id.lvRoutes);

        initializeContentLoader();
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME};
        int[] to = new int[]{R.id.tvRouteNum, R.id.tvRouteName};
        scRoutesAdapter = new SimpleCursorAdapter(this, R.layout.route_item, null, from, to, 0);
        scRoutesAdapter.setViewBinder(new RoutesAdapterViewBinder());
        lvRoutes.setAdapter(scRoutesAdapter);
        lvRoutes.setOnItemClickListener(routesListener);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private AdapterView.OnItemClickListener routesListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            route = new Route(
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                    scRoutesAdapter.getCursor().getShort(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION))!=0);

            Bundle bundle = new Bundle();
            bundle.putParcelable(Route.class.getCanonicalName(), route);
            Intent intentStations = new Intent(RoutesActivity.this, StationsActivity.class);
            intentStations.putExtra(Route.class.getCanonicalName(), bundle);
            startActivity(intentStations);
        }
    };

    private class RoutesAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.tvRouteNum){
                ((GradientDrawable)view.getBackground().getCurrent()).setColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR))));
                ((TextView)view).setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
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
        return new RoutesCursorLoader(this, databaseAccess);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scRoutesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        scRoutesAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

}