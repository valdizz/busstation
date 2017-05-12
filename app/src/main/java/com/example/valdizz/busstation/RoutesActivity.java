package com.example.valdizz.busstation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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


public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView lvRoutes;
    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRoutesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        lvRoutes = (ListView) findViewById(R.id.lvRoutes);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        String[] from = new String[]{"number", "name"};
        int[] to = new int[]{R.id.tvRouteNum, R.id.tvRouteName};
        scRoutesAdapter = new SimpleCursorAdapter(this, R.layout.route_item, null, from, to, 0);
        scRoutesAdapter.setViewBinder(new RoutesAdapterViewBinder());
        lvRoutes.setAdapter(scRoutesAdapter);
        getSupportLoaderManager().initLoader(0, null, this);

        lvRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(RoutesActivity.this, StationsActivity.class);
                intent.putExtra("route_id", String.valueOf(id));
                intent.putExtra("route_num", ((TextView)view.findViewById(R.id.tvRouteNum)).getText());
                intent.putExtra("route_name", ((TextView)view.findViewById(R.id.tvRouteName)).getText());
                startActivity(intent);
            }
        });
    }

    private class RoutesAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String color = cursor.getString(cursor.getColumnIndex("color"));
            String number = cursor.getString(cursor.getColumnIndex("number"));
            if (view.getId() == R.id.tvRouteNum){
                StateListDrawable drawable = (StateListDrawable)view.getBackground();
                GradientDrawable gradientDrawable = (GradientDrawable)drawable.getCurrent();
                gradientDrawable.mutate();
                gradientDrawable.setColor(Color.parseColor("#"+color));
                ((TextView)view).setText(number);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

    static class RoutesCursorLoader extends CursorLoader {
        DatabaseAccess db;

        public RoutesCursorLoader(Context context, DatabaseAccess db) {
            super(context);
            this.db = db;
        }

        @Override
        protected Cursor onLoadInBackground() {
            Cursor cursor = db.getRoutes();
            return cursor;
        }
    }


}