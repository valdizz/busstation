package com.valdizz.busstation;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.database.RoutesCursorLoader;
import com.valdizz.busstation.model.Route;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;


public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView lvRoutes;
    private DatabaseAccess databaseAccess;
    private SimpleCursorAdapter scRoutesAdapter;
    private FloatingActionMenu fam;
    private FloatingActionButton fabFavorites;
    private FloatingActionButton fabMap;
    private FloatingActionButton fabStations;
    private FloatingActionButton fabReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        lvRoutes = findViewById(R.id.lvRoutes);
        fam = findViewById(R.id.menu_fab);
        fabFavorites = findViewById(R.id.menu_favorites);
        fabMap = findViewById(R.id.menu_map);
        fabStations = findViewById(R.id.menu_stations);
        fabReminders = findViewById(R.id.menu_reminders);
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
        fabFavorites.setOnClickListener(onFabButtonClick());
        fabMap.setOnClickListener(onFabButtonClick());
        fabStations.setOnClickListener(onFabButtonClick());
        fabReminders.setOnClickListener(onFabButtonClick());
    }

    private View.OnClickListener onFabButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.menu_favorites:
                        fam.close(true);
                        startActivity(new Intent(RoutesActivity.this, FavoriteStationsActivity.class));
                        break;
                    case R.id.menu_stations:
                        fam.close(true);
                        startActivity(new Intent(RoutesActivity.this, FoundStationsActivity.class));
                        break;
                    case R.id.menu_map:
                        fam.close(true);
                        startActivity(new Intent(RoutesActivity.this, MapStationsActivity.class));
                        break;
                    case R.id.menu_reminders:
                        fam.close(true);
                        startActivity(new Intent(RoutesActivity.this, RemindersActivity.class));
                        break;
                }
            }
        };
    }

    private final AdapterView.OnItemClickListener routesListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            fam.close(true);
            Route route = new Route(
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                    scRoutesAdapter.getCursor().getString(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                    scRoutesAdapter.getCursor().getShort(scRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION)) != 0);

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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_menu:{
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new RoutesCursorLoader(this, databaseAccess);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        scRoutesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        scRoutesAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

}