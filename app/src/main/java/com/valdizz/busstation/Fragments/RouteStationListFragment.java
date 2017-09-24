package com.valdizz.busstation.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.valdizz.busstation.Database.DatabaseAccess;
import com.valdizz.busstation.Database.FavoriteStationsCursorLoader;
import com.valdizz.busstation.Database.FoundStationsCursorLoader;
import com.valdizz.busstation.Model.Route;
import com.valdizz.busstation.Model.Station;
import com.valdizz.busstation.R;
import com.valdizz.busstation.SheduleActivity;

public class RouteStationListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String FOUND_STATION_ACTIVITY = "FoundStationsActivity";

    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRouteStationAdapter;
    Station station;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.routestation_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeContentLoader();
        if (getActivity().getClass().getSimpleName().equals(FOUND_STATION_ACTIVITY)) {
            addUserFilter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getActivity().getClass().getSimpleName().equals(FOUND_STATION_ACTIVITY)){
            databaseAccess.open();
            getLoaderManager().getLoader(0).forceLoad();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(getActivity());
        databaseAccess.open();

        String[] from = new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME, DatabaseAccess.STATION_NAME};
        int[] to = new int[]{R.id.tvRSrouteNum, R.id.tvRSroute, R.id.tvRSstation};
        scRouteStationAdapter = new SimpleCursorAdapter(getActivity(), R.layout.routestation_item, null, from, to, 0);
        scRouteStationAdapter.setViewBinder(new RouteStationAdapterViewBinder());
        setListAdapter(scRouteStationAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    private void addUserFilter(){
        EditText userFilter = (EditText) getActivity().findViewById(R.id.userFilter);
        scRouteStationAdapter.getFilter().filter(userFilter.getText().toString());

        userFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scRouteStationAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        scRouteStationAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                databaseAccess.open();
                return databaseAccess.getFoundStations(new String[]{(constraint == null || constraint.length() == 0 ? "%%" : "%" + constraint.toString() + "%")});
            }
        });
    }

    private class RouteStationAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.tvRSrouteNum){
                ((GradientDrawable)view.getBackground().getCurrent()).setColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR))));
                ((TextView)view).setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
                return true;
            }
            return false;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Route route = new Route(
                scRouteStationAdapter.getCursor().getString(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                scRouteStationAdapter.getCursor().getString(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                scRouteStationAdapter.getCursor().getString(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                scRouteStationAdapter.getCursor().getShort(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION))!=0);
        station = new Station(
                scRouteStationAdapter.getCursor().getInt(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                route,
                scRouteStationAdapter.getCursor().getString(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                scRouteStationAdapter.getCursor().getShort(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE))!=0,
                scRouteStationAdapter.getCursor().getString(scRouteStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));

        Bundle bundle = new Bundle();
        bundle.putParcelable(Station.class.getCanonicalName(), station);
        Intent intentShedule = new Intent(getActivity(), SheduleActivity.class);
        intentShedule.putExtra(Station.class.getCanonicalName(), bundle);
        startActivity(intentShedule);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (getActivity().getClass().getSimpleName()){
            case FOUND_STATION_ACTIVITY:
                return new FoundStationsCursorLoader(getActivity(), databaseAccess, args);
            default:
                return new FavoriteStationsCursorLoader(getActivity(), databaseAccess);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scRouteStationAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        scRouteStationAdapter.swapCursor(null);
    }

}
